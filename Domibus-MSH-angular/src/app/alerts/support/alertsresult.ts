import {AlertsEntry} from "./alertsentry";

export interface AlertsResult {
  alertsEntries: Array<AlertsEntry>;
  count: number;
}
