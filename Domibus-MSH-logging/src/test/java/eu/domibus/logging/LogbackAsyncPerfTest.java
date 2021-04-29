package eu.domibus.logging;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;


/**
 * @author François Gautier
 * @since 5.0
 * <p>
 * Test the performance of async log of logback
 * <p>
 * use logback-test.xml in resources
 */
public class LogbackAsyncPerfTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(LogbackAsyncPerfTest.class);

    public static final int COUNT = 5000;

    public static final String RANDOM_TXT_2000 =
            "33862567472389492504775328715240672523225655346632319913511426976422390994756629190342237420457631076558" +
                    "909716136063473077884179675090337770571076891203600782915401142474875074142125857481504501409999" +
                    "983941242321674953788278742496442235984600793996072075198864963157672213602404899862123399096978" +
                    "092856567754551773655811829240242285546882256823614617506100031921918411436013660352017322720559" +
                    "480503678819897415380704327805367710281356434829757834443290923839523042145920850619555146194993" +
                    "581438753930558186951390442561052864783534817233480785698607159686964412099482986486490923567767" +
                    "117256200962359007546283227094573811757135867353485595749952320934386399055276260860507410822802" +
                    "588697028683512896941341713356773364351945705908030656294821105247835804842632742210772250025002" +
                    "570153808944544024119207849819376817010896079910100632821159590124805189172918120324981858295123" +
                    "585476018129662202099911962897894117922303385367357676707603921720559399324036168247325290568766" +
                    "805458942052078541631847338737955460071300524370651625428697763340974737059039716476225656443071" +
                    "558419217272670934974092687031585077833915426176222493752729398172505175844670982582018310810784" +
                    "385380840987987785039816583269665181561559940121803725322384203134021553423444314001671159080510" +
                    "518951450975032512516149614128292382427415122475903166878468556441265754099874631917365185976924" +
                    "719529808324772363487904233248809772094799446220608165539341869672929813736884160993486566847299" +
                    "361411351660054436218052216920416080681161606388981112705873397984537206746003615432277464978068" +
                    "193444838438287813118551435395798174449322389425501798420093714970970590339402333930911749820750" +
                    "130540617054511932421833387075304823043413370486704698066679566005809164552411705643184086864962" +
                    "839656215709452377702105874317468305907101071690679874416931471752220915995136888612960564140142" +
                    "550968047044403176608749433942206584906274469375345896768335241239064279451023949124412562916529" +
                    "399382638864572334946333083572532563940231798669918933387043643611302615";

    /**
     * Run 3 threads producing 10 000 log lines each (5 000 warn and 5 000 info).
     * In the case of queue overflow, the info line would be dropped.
     * <p>
     * generates a log file in target/log to check if no lines have been drop with current configuration.
     * With config:
     * <queueSize>1024</queueSize>
     * <discardingThreshold>0</discardingThreshold>
     * <p>
     * COUNT = 5 000
     * <p>
     * result: **** [30000 lines of log] [1.145 s] in file + console -> no log lost
     * <p>
     * 10 threads with long log lines:
     * <queueSize>3000</queueSize>
     * **** [100000] [5.948 s]
     */
    @Test
    public void latch() {
        long start = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(10);
        ExecutorService taskExecutor = Executors.newFixedThreadPool(10);
        List<Runnable> tasks = Arrays.asList(
                getRunnable(" -          ", latch),
                getRunnable(" --         ", latch),
                getRunnable(" ---        ", latch),
                getRunnable(" ----       ", latch),
                getRunnable(" -----      ", latch),
                getRunnable(" ------     ", latch),
                getRunnable(" -------    ", latch),
                getRunnable(" --------   ", latch),
                getRunnable(" ---------  ", latch),
                getRunnable(" ---------- ", latch));

        for (Runnable task : tasks) {
            taskExecutor.execute(task);
        }

        await().atMost(10, TimeUnit.SECONDS).until(() -> latch.getCount() == 0);

        long end = System.currentTimeMillis();
        LOG.error("\n**** [{}] [{} s]", tasks.size() * COUNT * 2, BigDecimal.valueOf(end - start).divide(BigDecimal.valueOf(1000), 3, RoundingMode.DOWN));
    }


    private Runnable getRunnable(String s, CountDownLatch latch) {
        return () -> printLog(s, latch);
    }

    private void printLog(String title, CountDownLatch latch) {
        for (int i = 1; i < COUNT + 1; i++) {
            LOG.info("info  [{} {}] {}", title, i, RANDOM_TXT_2000);
            LOG.warn("warn  [{} {}] {}", title, i, RANDOM_TXT_2000);
        }
        if (latch != null) {
            latch.countDown();
        }
    }
}
