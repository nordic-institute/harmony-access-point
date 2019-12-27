import {ErrorLogEntry} from "./errorlogentry";
export class ErrorLogResult {

  constructor(public errorLogEntries: Array<ErrorLogEntry>,
              public pageSize: number,
              public count: number,
              public filter: any,
              public mshRoles: Array<string>,
              public errorCodes: Array<string>) {
  }
}
