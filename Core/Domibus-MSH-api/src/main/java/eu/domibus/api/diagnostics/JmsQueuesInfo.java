package eu.domibus.api.diagnostics;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Breaz Ionut
 * @since 5.1.9
 */
public class JmsQueuesInfo {
    private List<QueueInfo> queues;

    public JmsQueuesInfo() {
        this.queues = new ArrayList<>();
    }

    public List<QueueInfo> getQueues() {
        return queues;
    }

    public void setQueues(List<QueueInfo> queues) {
        this.queues = queues;
    }

    public void addQueue(String name, long size) {
        this.queues.add(new QueueInfo(name, size));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("JMS Queues Information:\n");
        for (QueueInfo queue : queues) {
            sb.append("Queue: ").append(queue.getName()).append(", Size: ").append(queue.getSize()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Inner class to represent information about a single queue
     */
    public static class QueueInfo {
        private String name;
        private long size;

        public QueueInfo() {
        }

        public QueueInfo(String name, long size) {
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