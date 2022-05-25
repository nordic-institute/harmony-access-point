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
  nextAttemptTimezoneId: string;
  nextAttemptOffsetSeconds: number;
  parameters: string[];
  deleted: boolean;
}
