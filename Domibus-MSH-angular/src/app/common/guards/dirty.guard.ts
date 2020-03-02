import {Injectable} from '@angular/core';
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, CanDeactivate} from '@angular/router';
import {Observable} from 'rxjs/Observable';
import {MatDialog} from '@angular/material';
import {DialogsService} from '../dialogs/dialogs.service';

@Injectable()
export class DirtyGuard implements CanActivate, CanDeactivate<any> {

  constructor (public dialog: MatDialog, private dialogsService: DialogsService) {
  };

  canActivate (next: ActivatedRouteSnapshot,
               state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    return true;
  }

  canDeactivate (component: any, currentRoute: ActivatedRouteSnapshot,
                 currentState: RouterStateSnapshot,
                 nextState?: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    if (component.isDirty && !component.isDirty()) {
      return true;
    }
    return this.dialogsService.openCancelDialog();
  }
}
