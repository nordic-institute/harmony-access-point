import {Injectable, TemplateRef} from '@angular/core';
import {ComponentType} from '@angular/cdk/overlay';
import {MatDialog, MatDialogConfig, MatDialogRef} from '@angular/material';
import {YesNoDialogComponent} from './yes-no-dialog/yes-no-dialog.component';


@Injectable()
export class DialogsService {

  constructor(public dialog: MatDialog) {
  }

  public openSaveDialog(): Promise<boolean> {
    return this.openAndThen(YesNoDialogComponent, {
      data: {
        title: 'Do you want to save your changes?',
      }
    });
  }

  public openCancelDialog(): Promise<boolean> {
    return this.openAndThen(YesNoDialogComponent, {
      data: {
        title: 'Do you want to cancel all unsaved operations?',
      }
    });
  }

  public openYesNoDialog(): Promise<boolean> {
    return this.openAndThen(YesNoDialogComponent);
  }

  public open<T, D = any, R = any>(dialog: ComponentType<T> | TemplateRef<T>, config?: MatDialogConfig<D>): MatDialogRef<T, R> {
    return this.dialog.open(dialog);
  }

  // public openAndThen2<T, D = any, R = any>(dialog: ComponentType<T> | TemplateRef<T>, config?: MatDialogConfig<D>): Observable<R | undefined> {
  //   return this.dialog.open(dialog).afterClosed();
  // }

  public openAndThen<T, D = any, R = any>(dialog: ComponentType<T> | TemplateRef<T>, config?: MatDialogConfig<D>): Promise<R | undefined> {
    return this.dialog.open(dialog, config).afterClosed().toPromise();
  }


}
