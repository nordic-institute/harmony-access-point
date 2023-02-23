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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static eu.domibus.core.spring.DomibusContextRefreshedListener.SYNC_LOCK_KEY;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class SynchronizedRunnableIT extends AbstractIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(SynchronizedRunnableIT.class);
    public static final String SELECT_ALL_FROM_TABLE = "select mpc from MpcEntity mpc";
    public static final String SELECT_FROM_TABLE_WHERE_VALUE_IN_LIST = "select mpc from MpcEntity mpc where value in :TEST_VALUES";
    public static final String DELETE_TEST_DATA = "delete from MpcEntity where value in :TEST_VALUES";

    @Autowired
    private SynchronizedRunnableFactory synchronizedRunnableFactory;

    @Autowired
    private TransactionalTestService testService;

    @Test
    @Transactional
    public void blocksWithTimeout() {
        AtomicInteger atomicInteger = new AtomicInteger();
        Runnable task1 = () -> {
            LOG.info("Task 1 enter");
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                LOG.error("SynchronizedRunnableIT stopped", e);
            }
            atomicInteger.getAndIncrement();
            LOG.info("Task 1 exit");
        };

        SynchronizedRunnable synchronizedRunnable = synchronizedRunnableFactory.synchronizedRunnable(task1, SYNC_LOCK_KEY);
        Thread t1 = new Thread(synchronizedRunnable);
        t1.start();

        Runnable task2 = () -> {
            LOG.info("Task 2 enter");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                LOG.error("SynchronizedRunnableIT stopped", e);
            }
            atomicInteger.getAndIncrement();
            LOG.info("Task 2 exit");
            Assert.fail();
        };
        SynchronizedRunnable synchronizedRunnable2 = synchronizedRunnableFactory.synchronizedRunnable(task2, SYNC_LOCK_KEY);
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
            Assert.assertEquals(1, atomicInteger.get());
        } catch (InterruptedException e) {
            LOG.error("SynchronizedRunnableIT stopped", e);
        }
    }

    @Test
    @Transactional
    public void allTasksSucceed() {
        AtomicInteger i = new AtomicInteger();
        Runnable task1 = () -> {
            LOG.info("Task 1 enter");
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                LOG.error("SynchronizedRunnableIT stopped", e);
            }
            i.getAndIncrement();
            LOG.info("Task 1 exit");
        };

        SynchronizedRunnable synchronizedRunnable = synchronizedRunnableFactory.synchronizedRunnable(task1, SYNC_LOCK_KEY);
        Thread t1 = new Thread(synchronizedRunnable);
        t1.start();

        Runnable task2 = () -> {
            LOG.info("Task 2 enter");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOG.error("SynchronizedRunnableIT stopped", e);
            }
            i.getAndIncrement();
            LOG.info("Task 2 exit");
        };
        SynchronizedRunnable synchronizedRunnable2 = synchronizedRunnableFactory.synchronizedRunnable(task2, SYNC_LOCK_KEY);
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
            Assert.assertEquals(2, i.get());
        } catch (InterruptedException e) {
            LOG.error("SynchronizedRunnableIT stopped", e);
        }
    }

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

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
    final String valueFromOwnerThread = "from-owner-thread";
    final String valueFromTask1 = "from-task_1-thread";
    final String valueFromTask2 = "from-task_2-thread";

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
                changeDatabase(valueFromOwnerThread);
                runTwoThreads(() -> {
                            LOG.info("Task 1 enter and does a DB change");
                            changeDatabase(valueFromTask1);
                            LOG.info("Task 1 exit");
                        }, () -> {
                            LOG.info("Task 2 enter and does a DB change");
                            changeDatabase(valueFromTask2);
                            LOG.info("Task 2 exit");
                        }
                );
            }
        });
        //then
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseContainsOnly(valueFromOwnerThread, valueFromTask1, valueFromTask2);
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
                changeDatabase(valueFromOwnerThread);
                runTwoThreads(() -> {
                    LOG.info("Task 1 enter and does a DB change");
                    changeDatabase(valueFromTask1);
                    LOG.info("Task 1 exit");
                }, () -> {
                    LOG.info("Task 2 enter and does a DB change");
                    changeDatabase(valueFromTask2);
                    LOG.info("Task 2 throws NoResultException");
                    throw new NoResultException("Task 2 exception");
                });
            }
        });
        //then
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseContainsOnly(valueFromOwnerThread, valueFromTask1);
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
                changeDatabase(valueFromOwnerThread);
                runTwoThreads(() -> {
                            LOG.info("Task 1 enter and does a DB change");
                            changeDatabase(valueFromTask1);
                            LOG.info("Task 1 exit");
                        }, () -> {
                            LOG.info("Task 2 enter and does a DB change");
                            changeDatabase(valueFromTask2);
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
                assertDatabaseContainsOnly(valueFromOwnerThread, valueFromTask1, valueFromTask2);
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
                changeDatabase(valueFromOwnerThread);
                runTwoThreads(() -> {
                            LOG.info("Task 1 enter and does a DB change");
                            changeDatabase(valueFromTask1);
                            LOG.info("Task 1 exit");
                        }, () -> {
                            LOG.info("Task 2 enter and does a DB change");
                            changeDatabase(valueFromTask2);
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
                assertDatabaseContainsOnly(valueFromTask1);
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
                    changeDatabase(valueFromOwnerThread);
                    runTwoThreads(() -> {
                                LOG.info("Task 1 enter and does a DB change");
                                changeDatabase(valueFromTask1);
                                LOG.info("Task 1 exit");
                            }, () -> {
                                LOG.info("Task 2 enter and does a DB change");
                                changeDatabase(valueFromTask2);
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
                assertDatabaseContainsOnly(valueFromTask1, valueFromTask2);
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
        runTwoThreads(() -> {
                    LOG.info("Task 1 enter and does a DB change");
                    changeDatabase(valueFromTask1);
                    LOG.info("Task 1 exit");
                }, () -> {
                    LOG.info("Task 2 enter and does a DB change");
                    changeDatabase(valueFromTask2);
                    LOG.info("Task 2 exit");
                }
        );
        //then
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseContainsOnly(valueFromTask1, valueFromTask2);
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
        runTwoThreads(() -> {
                    LOG.info("Task 1 enter and does a DB change");
                    changeDatabase(valueFromTask1);
                    LOG.info("Task 1 exit");
                }, () -> {
                    LOG.info("Task 2 enter and does a DB change");
                    changeDatabase(valueFromTask2);
                    LOG.info("Task 2 throws exception");
                    throw new NoResultException("Task 2 exception");
                }
        );
        //then
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseContainsOnly(valueFromTask1);
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
        runTwoThreads(() -> {
                    LOG.info("Task 1 enter and does a DB change");
                    changeDatabase(valueFromTask1);
                    LOG.info("Task 1 exit");
                }, () -> {
                    LOG.info("Task 2 enter and does a DB change");
                    changeDatabase(valueFromTask2);
                    LOG.info("Task 2 throws exception");
                    throw new RuntimeException("Task 2 exception");
                }
        );
        //then
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertDatabaseContainsOnly(valueFromTask1, valueFromTask2);
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
        runTwoThreads(() -> {
                    LOG.info("Task 1 enter and does a DB change");
                    changeDatabase(valueFromTask1);
                    LOG.info("Task 1 exit");
                }, () -> {
                    LOG.info("Task 2 enter and does a DB change");
                    changeDatabase(valueFromTask2);
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
                assertDatabaseContainsOnly(valueFromTask1);
            }
        });
    }

    private void runTwoThreads(Runnable task1, Runnable task2) {
        SynchronizedRunnable synchronizedRunnable = synchronizedRunnableFactory.synchronizedRunnable(task1, SYNC_LOCK_KEY);
        Thread t1 = new Thread(synchronizedRunnable);
        t1.start();

        SynchronizedRunnable synchronizedRunnable2 = synchronizedRunnableFactory.synchronizedRunnable(task2, SYNC_LOCK_KEY);
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

    private void assertDatabaseIsClean() {
        TypedQuery<MpcEntity> query = em.createQuery(SELECT_ALL_FROM_TABLE, MpcEntity.class);
        List<MpcEntity> resultList = query.getResultList();
        assertTrue("Expecting empty table but found: " + resultList, resultList.isEmpty());
    }

    private void assertDatabaseContainsOnly(String... values) {
        TypedQuery<MpcEntity> query = em.createQuery(SELECT_FROM_TABLE_WHERE_VALUE_IN_LIST, MpcEntity.class);
        query.setParameter("TEST_VALUES", Arrays.asList(values));
        List<MpcEntity> resultList = query.getResultList();
        List<String> valuesInDb = resultList.stream().map(MpcEntity::getValue).collect(Collectors.toList());
        MatcherAssert.assertThat(valuesInDb, containsInAnyOrder(values));
    }

    private void changeDatabase(String newValue) {
        MpcEntity newEntity = new MpcEntity();
        newEntity.setValue(newValue);
        newEntity.setCreatedBy(SynchronizedRunnableIT.class.getName());
        em.persist(newEntity);
    }

    private void cleanDatabase() {
        Query query = em.createQuery(DELETE_TEST_DATA);
        query.setParameter("TEST_VALUES", Arrays.asList(valueFromOwnerThread, valueFromTask1, valueFromTask2));
        query.executeUpdate();
    }

}
