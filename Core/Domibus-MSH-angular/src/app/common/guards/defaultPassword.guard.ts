import {Injectable} from '@angular/core';
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, CanDeactivate} from '@angular/router';
import {Observable} from 'rxjs/Observable';
import {MatDialog} from '@angular/material';
import {SecurityService} from '../../security/security.service';

@Injectable()
export class DefaultPasswordGuard implements CanActivate, CanDeactivate<any> {

  constructor(public dialog: MatDialog, private securityService: SecurityService) {
  };

  async canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

    await this.securityService.isAppInitialized();

    return !this.securityService.mustChangePassword();

  }

  canDeactivate(component: any, currentRoute: ActivatedRouteSnapshot,
                currentState: RouterStateSnapshot,
                nextState?: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {

    return true;

  }

}
