package eu.domibus.core;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.MpcEntity;
import eu.domibus.api.multitenancy.lock.SynchronizedRunnable;
import eu.domibus.api.multitenancy.lock.SynchronizedRunnableFactory;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.mock.TransactionalTestService;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static eu.domibus.core.spring.DomibusApplicationContextListener.SYNC_LOCK_KEY;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.*;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class SynchronizedRunnableIT extends AbstractIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(SynchronizedRunnableIT.class);
    public static final String SELECT_FROM_TABLE_WHERE_VALUE_IN_LIST = "select mpc from MpcEntity mpc where value in :TEST_VALUES";
    public static final String DELETE_TEST_DATA = "delete from MpcEntity where value in :TEST_VALUES";
    private static final String SCHEDULER_SYNCHRONIZATION_LOCK = "scheduler-synchronization.lock";

    @Autowired
    private SynchronizedRunnableFactory synchronizedRunnableFactory;

    @Autowired
    private TransactionalTestService testService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;
    
    private final String VALUE_FROM_OWNER_THREAD = "from-owner-thread";
    private final String VALUE_FROM_TASK_1 = "from-task_1-thread";
    private final String VALUE_FROM_TASK_2 = "from-task_2-thread";

    @Test
    @Transactional
    public void blocksWithTimeout() {
        AtomicInteger atomicInteger = new AtomicInteger();
        runTwoThreads(
                true, () -> {
                    LOG.info("Task 1 enter");
                    try {
                        Thread.sleep(10000);
                    } catch (Exception e) {
                        LOG.error("SynchronizedRunnableIT stopped", e);
                    }
                    atomicInteger.getAndIncrement();
                    LOG.info("Task 1 exit");
                }, () -> {
                    LOG.info("Task 2 enter");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        LOG.error("SynchronizedRunnableIT stopped", e);
                    }
                    atomicInteger.getAndIncrement();
                    LOG.info("Task 2 exit");
                    Assert.fail();
                }
        );

        Assert.assertEquals(1, atomicInteger.get());
    }

    @Test
    @Transactional
    public void allTasksSucceed() {
        AtomicInteger i = new AtomicInteger();
        runTwoThreads(
                true, () -> {
                    LOG.info("Task 1 enter");
                    try {
                        Thread.sleep(2000);
                    } catch (Exception e) {
                        LOG.error("SynchronizedRunnableIT stopped", e);
                    }
                    i.getAndIncrement();
                    LOG.info("Task 1 exit");
                }, () -> {
                    LOG.info("Task 2 enter");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        LOG.error("SynchronizedRunnableIT stopped", e);
                    }
                    i.getAndIncrement();
                    LOG.info("Task 2 exit");
                }
        );
        Assert.assertEquals(2, i.get());
    }

    @Before
    public void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @After
    public void cleanup(){
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                cleanDatabase();
                assertDatabaseIsClean();
            }
        });
    }

    @Test
    public void whenOwnerThreadStartsTransactionAndNoExceptionsThenNoRollback() {
        //given
        assertFalse("This test expects to not be in an active transaction", TransactionSynchronizationManager.isActualTransactionActive());
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseIsClean();
            }
        });
        //when
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                LOG.info("Owner thread begins transaction and does a DB change");
                changeDatabase(VALUE_FROM_OWNER_THREAD);
                runTwoThreads(true, () -> {
                            LOG.info("Task 1 enter and does a DB change");
                            changeDatabase(VALUE_FROM_TASK_1);
                            LOG.info("Task 1 exit");
                        }, () -> {
                            LOG.info("Task 2 enter and does a DB change");
                            changeDatabase(VALUE_FROM_TASK_2);
                            LOG.info("Task 2 exit");
                        }
                );
            }
        });
        //then
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseContainsOnly(VALUE_FROM_OWNER_THREAD, VALUE_FROM_TASK_1, VALUE_FROM_TASK_2);
            }
        });
    }

    @Test
    public void whenOwnerThreadStartsTransactionAndChildNoResultExceptionThenRollbackOnlyThatChild() {
        //given
        assertFalse("This test expects to not be in an active transaction", TransactionSynchronizationManager.isActualTransactionActive());
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseIsClean();
            }
        });
        //when
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                LOG.info("Owner thread begins transaction and does a DB change");
                changeDatabase(VALUE_FROM_OWNER_THREAD);
                runTwoThreads(true, () -> {
                    LOG.info("Task 1 enter and does a DB change");
                    changeDatabase(VALUE_FROM_TASK_1);
                    LOG.info("Task 1 exit");
                }, () -> {
                    LOG.info("Task 2 enter and does a DB change");
                    changeDatabase(VALUE_FROM_TASK_2);
                    LOG.info("Task 2 throws NoResultException");
                    throw new NoResultException("Task 2 exception");
                });
            }
        });
        //then
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseContainsOnly(VALUE_FROM_OWNER_THREAD, VALUE_FROM_TASK_1);
            }
        });
    }

    @Test
    public void whenOwnerThreadStartsTransactionAndChildExceptionThenNoRollback() {
        //given
        assertFalse("This test expects to not be in an active transaction", TransactionSynchronizationManager.isActualTransactionActive());
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseIsClean();
            }
        });
        //when
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                LOG.info("Owner thread begins transaction and does a DB change");
                changeDatabase(VALUE_FROM_OWNER_THREAD);
                runTwoThreads(true, () -> {
                            LOG.info("Task 1 enter and does a DB change");
                            changeDatabase(VALUE_FROM_TASK_1);
                            LOG.info("Task 1 exit");
                        }, () -> {
                            LOG.info("Task 2 enter and does a DB change");
                            changeDatabase(VALUE_FROM_TASK_2);
                            LOG.info("Task 2 throws RuntimeException");
                            throw new RuntimeException("Task 2 exception");
                        }
                );
            }
        });
        //then
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseContainsOnly(VALUE_FROM_OWNER_THREAD, VALUE_FROM_TASK_1, VALUE_FROM_TASK_2);
            }
        });
    }

    @Test
    public void whenOwnerThreadStartsTransactionAndChildExceptionFromTransactionalMethodThenRollbackOnlyChild() {
        //given
        assertFalse("This test expects to not be in an active transaction", TransactionSynchronizationManager.isActualTransactionActive());
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseIsClean();
            }
        });
        //when
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                LOG.info("Owner thread begins transaction and does a DB change");
                changeDatabase(VALUE_FROM_OWNER_THREAD);
                runTwoThreads(true, () -> {
                            LOG.info("Task 1 enter and does a DB change");
                            changeDatabase(VALUE_FROM_TASK_1);
                            LOG.info("Task 1 exit");
                        }, () -> {
                            LOG.info("Task 2 enter and does a DB change");
                            changeDatabase(VALUE_FROM_TASK_2);
                            LOG.info("Task 2 throws RuntimeException");
                            testService.doInTransactionalMethod(() -> {
                                throw new RuntimeException("Task 2 exception");
                            });
                        }
                );
            }
        });
        //then
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseContainsOnly(VALUE_FROM_OWNER_THREAD, VALUE_FROM_TASK_1);
            }
        });
    }

    @Test
    public void whenOwnerThreadStartsTransactionAndOwnerExceptionThenRollbackOnlyOwner() {
        //given
        assertFalse("This test expects to not be in an active transaction", TransactionSynchronizationManager.isActualTransactionActive());
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseIsClean();
            }
        });
        //when
        try {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    LOG.info("Owner thread begins transaction and does a DB change");
                    changeDatabase(VALUE_FROM_OWNER_THREAD);
                    runTwoThreads(true, () -> {
                                LOG.info("Task 1 enter and does a DB change");
                                changeDatabase(VALUE_FROM_TASK_1);
                                LOG.info("Task 1 exit");
                            }, () -> {
                                LOG.info("Task 2 enter and does a DB change");
                                changeDatabase(VALUE_FROM_TASK_2);
                                LOG.info("Task 2 exit");
                            }
                    );
                    throw new RuntimeException("Owner throws exception");
                }
            });
        } catch (RuntimeException e){
            LOG.info("Caught exception: " + e.getMessage());
        }
        //then
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseContainsOnly(VALUE_FROM_TASK_1, VALUE_FROM_TASK_2);
            }
        });
    }

    @Test
    public void whenChildStartsTransactionAndNoExceptionsThenNoRollback() {
        //given
        assertFalse("This test expects to not be in an active transaction", TransactionSynchronizationManager.isActualTransactionActive());
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseIsClean();
            }
        });
        //when
        runTwoThreads(true, () -> {
                    LOG.info("Task 1 enter and does a DB change");
                    changeDatabase(VALUE_FROM_TASK_1);
                    LOG.info("Task 1 exit");
                }, () -> {
                    LOG.info("Task 2 enter and does a DB change");
                    changeDatabase(VALUE_FROM_TASK_2);
                    LOG.info("Task 2 exit");
                }
        );
        //then
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseContainsOnly(VALUE_FROM_TASK_1, VALUE_FROM_TASK_2);
            }
        });
    }

    @Test
    public void whenChildStartsTransactionAndNoResultExceptionThenRollbackOnlyChild() {
        //given
        assertFalse("This test expects to not be in an active transaction", TransactionSynchronizationManager.isActualTransactionActive());
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseIsClean();
            }
        });
        //when
        runTwoThreads(true, () -> {
                    LOG.info("Task 1 enter and does a DB change");
                    changeDatabase(VALUE_FROM_TASK_1);
                    LOG.info("Task 1 exit");
                }, () -> {
                    LOG.info("Task 2 enter and does a DB change");
                    changeDatabase(VALUE_FROM_TASK_2);
                    LOG.info("Task 2 throws exception");
                    throw new NoResultException("Task 2 exception");
                }
        );
        //then
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseContainsOnly(VALUE_FROM_TASK_1);
            }
        });
    }

    @Test
    public void whenChildStartsTransactionAndExceptionThenNoRollback() {
        //given
        assertFalse("This test expects to not be in an active transaction", TransactionSynchronizationManager.isActualTransactionActive());
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseIsClean();
            }
        });
        //when
        runTwoThreads(true, () -> {
                    LOG.info("Task 1 enter and does a DB change");
                    changeDatabase(VALUE_FROM_TASK_1);
                    LOG.info("Task 1 exit");
                }, () -> {
                    LOG.info("Task 2 enter and does a DB change");
                    changeDatabase(VALUE_FROM_TASK_2);
                    LOG.info("Task 2 throws exception");
                    throw new RuntimeException("Task 2 exception");
                }
        );
        //then
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseContainsOnly(VALUE_FROM_TASK_1, VALUE_FROM_TASK_2);
            }
        });
    }

    @Test
    public void whenChildStartsTransactionAndExceptionFromTransactionalMethodThenRollbackOnlyThatChild() {
        //given
        assertFalse("This test expects to not be in an active transaction", TransactionSynchronizationManager.isActualTransactionActive());
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseIsClean();
            }
        });
        //when
        runTwoThreads(true, () -> {
                    LOG.info("Task 1 enter and does a DB change");
                    changeDatabase(VALUE_FROM_TASK_1);
                    LOG.info("Task 1 exit");
                }, () -> {
                    LOG.info("Task 2 enter and does a DB change");
                    changeDatabase(VALUE_FROM_TASK_2);
                    LOG.info("Task 2 throws exception");
                    testService.doInTransactionalMethod(() -> {
                        throw new RuntimeException("Task 2 exception");
                    });
                }
        );
        //then
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseContainsOnly(VALUE_FROM_TASK_1);
            }
        });
    }

    @Test
    public void whenParallelChildStartsTransactionAndExceptionFromTransactionalMethodThenRollbackOnlyThatChild() {
        //given
        assertFalse("This test expects to not be in an active transaction", TransactionSynchronizationManager.isActualTransactionActive());
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseIsClean();
            }
        });
        //when
        runTwoThreadsInParallel(() -> {
                    LOG.info("Task 1 enter and does a DB change");
                    changeDatabase(VALUE_FROM_TASK_1);
                    LOG.info("Task 1 exit");
                }, () -> {
                    LOG.info("Task 2 enter and does a DB change");
                    changeDatabase(VALUE_FROM_TASK_2);
                    LOG.info("Task 2 throws exception");
                    testService.doInTransactionalMethod(() -> {
                        throw new RuntimeException("Task 2 exception");
                    });
                }
        );
        //then
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseContainsOnly(VALUE_FROM_TASK_1);
            }
        });
    }

    @Test
    public void whenParallelChildStartsTransactionAndExceptionThenNoRollback() {
        //given
        assertFalse("This test expects to not be in an active transaction", TransactionSynchronizationManager.isActualTransactionActive());
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseIsClean();
            }
        });
        //when
        runTwoThreadsInParallel(() -> {
                    LOG.info("Task 1 enter and does a DB change");
                    changeDatabase(VALUE_FROM_TASK_1);
                    LOG.info("Task 1 exit");
                }, () -> {
                    LOG.info("Task 2 enter and does a DB change");
                    changeDatabase(VALUE_FROM_TASK_2);
                    LOG.info("Task 2 throws exception");
                    throw new RuntimeException("Task 2 exception");
                }
        );
        //then
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseContainsOnly(VALUE_FROM_TASK_1, VALUE_FROM_TASK_2);
            }
        });
    }

    @Test
    public void whenParallelChildStartsTransactionAndNoResultExceptionThenRollbackOnlyThatChild() {
        //given
        assertFalse("This test expects to not be in an active transaction", TransactionSynchronizationManager.isActualTransactionActive());
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseIsClean();
            }
        });
        //when
        runTwoThreadsInParallel(() -> {
                    LOG.info("Task 1 enter and does a DB change");
                    changeDatabase(VALUE_FROM_TASK_1);
                    LOG.info("Task 1 exit");
                }, () -> {
                    LOG.info("Task 2 enter and does a DB change");
                    changeDatabase(VALUE_FROM_TASK_2);
                    LOG.info("Task 2 throws exception");
                    throw new NoResultException("Task 2 exception");
                }
        );
        //then
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseContainsOnly(VALUE_FROM_TASK_1);
            }
        });
    }

    /**
     * Runs two threads using SynchronizedRunnable infrastructure
     * @param useSameLock when true, SynchronizedRunnable will wait until the lock is released
     * @param task1
     * @param task2
     */
    private void runTwoThreads(boolean useSameLock, Runnable task1, Runnable task2) {
        SynchronizedRunnable synchronizedRunnable = synchronizedRunnableFactory.synchronizedRunnable(task1, SYNC_LOCK_KEY);
        Thread t1 = new Thread(synchronizedRunnable);
        t1.start();

        SynchronizedRunnable synchronizedRunnable2 = synchronizedRunnableFactory.synchronizedRunnable(task2, useSameLock ? SYNC_LOCK_KEY : SCHEDULER_SYNCHRONIZATION_LOCK);
        Thread t2 = new Thread(synchronizedRunnable2);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOG.error("SynchronizedRunnableIT stopped", e);
        }
        LOG.info("Launch task 2");
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            LOG.error("SynchronizedRunnableIT stopped", e);
        }
    }

    private void runTwoThreadsInParallel(Runnable task1, Runnable task2) {
        AtomicBoolean isTaskOneStarted = new AtomicBoolean(false);
        AtomicBoolean isTaskTwoStarted = new AtomicBoolean(false);
        Runnable parallelTask1 = () -> {
            LOG.info("Task 1 started");
            isTaskOneStarted.getAndSet(true);
            LOG.info("Task 1 ready and waiting for Task 2");
            await().untilTrue(isTaskTwoStarted);
            task1.run();
        };
        Runnable parallelTask2 = () -> {
            LOG.info("Task 2 started");
            isTaskTwoStarted.getAndSet(true);
            LOG.info("Task 2 ready and waiting for Task 1");
            await().untilTrue(isTaskOneStarted);
            task2.run();
        };
        runTwoThreads(false, parallelTask1, parallelTask2);
    }

    private void assertDatabaseIsClean() {
        List<String> valuesInDb = getAllValuesFromThisTest();
        MatcherAssert.assertThat("Expecting the table to not contain any data related to this test", valuesInDb, empty());
    }

    private void assertDatabaseContainsOnly(String... values) {
        List<String> valuesInDb = getAllValuesFromThisTest();
        MatcherAssert.assertThat(valuesInDb, containsInAnyOrder(values));
    }

    private List<String> getAllValuesFromThisTest() {
        TypedQuery<MpcEntity> query = em.createQuery(SELECT_FROM_TABLE_WHERE_VALUE_IN_LIST, MpcEntity.class);
        query.setParameter("TEST_VALUES", Arrays.asList(VALUE_FROM_OWNER_THREAD, VALUE_FROM_TASK_1, VALUE_FROM_TASK_2));
        List<MpcEntity> resultList = query.getResultList();
        return resultList.stream().map(MpcEntity::getValue).collect(Collectors.toList());
    }

    private void changeDatabase(String newValue) {
        MpcEntity newEntity = new MpcEntity();
        newEntity.setValue(newValue);
        newEntity.setCreatedBy(SynchronizedRunnableIT.class.getName());
        em.persist(newEntity);
    }

    private void cleanDatabase() {
        Query query = em.createQuery(DELETE_TEST_DATA);
        query.setParameter("TEST_VALUES", Arrays.asList(VALUE_FROM_OWNER_THREAD, VALUE_FROM_TASK_1, VALUE_FROM_TASK_2));
        query.executeUpdate();
    }

}
