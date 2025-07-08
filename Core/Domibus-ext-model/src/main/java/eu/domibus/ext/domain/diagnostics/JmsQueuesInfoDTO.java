package eu.domibus.ext.domain.diagnostics;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for JMS queues information
 *
 * @author Breaz Ionut
 * @since 5.1.9
 */
public class JmsQueuesInfoDTO {
    private List<QueueInfoDTO> queues;

    public JmsQueuesInfoDTO() {
        this.queues = new ArrayList<>();
    }

    public List<QueueInfoDTO> getQueues() {
        return queues;
    }

    public void setQueues(List<QueueInfoDTO> queues) {
        this.queues = queues;
    }

    public void addQueue(String name, long size) {
        this.queues.add(new QueueInfoDTO(name, size));
    }

    public static class QueueInfoDTO {
        private String name;
        private long size;

        public QueueInfoDTO() {
        }

        public QueueInfoDTO(String name, long size) {
            this.name = name;
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }
    }
}