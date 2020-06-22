export interface AlertsEntry {
  processed: boolean;
  alertId: string;
  alertType: string;
  alertLevel: string;
  alertStatus: string;
  attempts: number;
  maxAttempts: number;
  creationTime: Date;
  reportingTime: Date;
  reportingTimeFailure: Date;
  nextAttempt: Date;
  parameters: string[];
  deleted: boolean;
}
