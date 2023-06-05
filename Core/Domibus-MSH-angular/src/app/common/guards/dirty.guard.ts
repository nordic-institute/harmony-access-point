import {Injectable} from '@angular/core';
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, CanDeactivate} from '@angular/router';
import {Observable} from 'rxjs/Observable';
import {MatDialog} from '@angular/material';
import {DialogsService} from '../dialogs/dialogs.service';
import {SecurityService} from '../../security/security.service';
import {SessionState} from '../../security/SessionState';
import {SessionService} from '../../security/session.service';
import {instanceOfModifiableList} from '../mixins/type.utils';
import {ApplicationContextService} from '../application-context.service';

@Injectable()
export class DirtyGuard implements CanDeactivate<any> {

  constructor(private securityService: SecurityService) {
  };

  async canDeactivate(component: any, currentRoute: ActivatedRouteSnapshot, currentState: RouterStateSnapshot, nextState?: RouterStateSnapshot) {
    if (currentState.url == nextState.url) {
      return true;
    }
    return this.securityService.canAbandonUnsavedChanges(component);
  }

}
