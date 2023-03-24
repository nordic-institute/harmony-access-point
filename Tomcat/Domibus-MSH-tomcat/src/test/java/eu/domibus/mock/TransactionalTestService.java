package eu.domibus.mock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionalTestService {
    @Transactional
    public void doInTransactionalMethod(Runnable action){
        action.run();
    }
}
