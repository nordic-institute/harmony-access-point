import {Injectable} from '@angular/core';
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, CanDeactivate} from '@angular/router';
import {Observable} from 'rxjs/Observable';
import {MatDialog} from '@angular/material';
import {DialogsService} from '../dialogs/dialogs.service';
import {SecurityService} from '../../security/security.service';
import {SessionState} from '../../security/SessionState';
import {SessionService} from '../../security/session.service';

@Injectable()
export class DirtyGuard implements CanActivate, CanDeactivate<any> {

  constructor(public dialog: MatDialog, private dialogsService: DialogsService,
              private securityService: SecurityService, private sessionService: SessionService,) {
  };

  canActivate(next: ActivatedRouteSnapshot,
              state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    return true;
  }

  async canDeactivate(component: any, currentRoute: ActivatedRouteSnapshot,
                      currentState: RouterStateSnapshot, nextState?: RouterStateSnapshot) {

    const session: SessionState = this.sessionService.getCurrentSession();
    if (session !== SessionState.ACTIVE) {
      return true;
    }
    if (!this.securityService.getCurrentUser()) {
      return true;
    }
    const isAuthenticated = await this.securityService.isAuthenticated();
    if (!isAuthenticated) {
      return true;
    }

    if (component.isDirty && !component.isDirty()) {
      return true;
    }
    return this.dialogsService.openCancelDialog();
  }
}
