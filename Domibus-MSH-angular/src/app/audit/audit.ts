import {HttpParams} from '@angular/common/http';

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Rest entry point to retrieve the audit logs.
 */
export class AuditResponseRo {
  id: string;
  revisionId: string;
  auditTargetName: string;
  action: string;
  user: string;
  changed: string;

  constructor(id: string, revisionId: string, auditTargetName: string, action: string, user: string, changed: string) {
    this.id = id;
    this.revisionId = revisionId;
    this.auditTargetName = auditTargetName;
    this.action = action;
    this.user = user;
    this.changed = changed;
  }
}

export class AuditCriteria {
  auditTargetName: string[];
  action: string[];
  user: string[];
  from;
  to;
  start;
  max;

  public toURLSearchParams(): HttpParams {
    let searchParams = new HttpParams();

    if (this.auditTargetName) {
      this.auditTargetName.forEach(el => searchParams.append('auditTargetName', el));
    }
    if (this.action) {
      this.action.forEach(el => searchParams.append('action', el));
    }
    if (this.user) {
      this.user.forEach(el => searchParams.append('user', el));
    }
    if (this.from) {
      searchParams = searchParams.append('from', this.from.toISOString());
    }
    if (this.to) {
      searchParams = searchParams.append('to', this.to.toISOString());
    }
    return searchParams;
  }
}
