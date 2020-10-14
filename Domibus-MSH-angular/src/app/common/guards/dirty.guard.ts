import {Injectable} from '@angular/core';
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, CanDeactivate} from '@angular/router';
import {Observable} from 'rxjs/Observable';
import {MatDialog} from '@angular/material';
import {DialogsService} from '../dialogs/dialogs.service';
import {SecurityService} from '../../security/security.service';
import {SessionState} from '../../security/SessionState';
import {SessionService} from '../../security/session.service';
import {instanceOfModifiableList} from '../mixins/type.utils';

@Injectable()
export class DirtyGuard implements CanActivate, CanDeactivate<any> {

  constructor(public dialog: MatDialog, private dialogsService: DialogsService, private securityService: SecurityService) {
  };

  canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    return true;
  }

  async canDeactivate(component: any, currentRoute: ActivatedRouteSnapshot, currentState: RouterStateSnapshot, nextState?: RouterStateSnapshot) {

    if (!component) {
      return true;
    }

    if (!instanceOfModifiableList(component)) {
      return true;
    }

    const canBypassCheckDirty = await this.securityService.canBypassCheckDirty();
    if (canBypassCheckDirty) {
      return true;
    }

    if (!component.isDirty()) {
      return true;
    }

    return this.dialogsService.openCancelDialog();
  }

}
