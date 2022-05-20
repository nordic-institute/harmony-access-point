import {Injectable, TemplateRef} from '@angular/core';
import {ComponentType} from '@angular/cdk/overlay';
import {MatDialog, MatDialogConfig, MatDialogRef} from '@angular/material';
import {YesNoDialogComponent} from './yes-no-dialog/yes-no-dialog.component';

@Injectable()
export class DialogsService {

  constructor(public dialog: MatDialog) {
  }

  public openResendDialog(): Promise<boolean> {
    return this.openYesNoDialogDialog({
      data: {
        title: 'Do you want to resend the selected message?\n Click on "Resend" to proceed or "Cancel" to abort.',
        yesText: 'Resend',
        yesIcon: 'send',
        noText: 'Cancel'
      }
    });
  }

  public openRestoreDialog(): Promise<boolean> {
    return this.openYesNoDialogDialog({
      data: {
        title: 'Do you want to restore for the selected version? Changes will be applied immediately.',
      }
    });
  }

  public openSaveDialog(): Promise<boolean> {
    return this.openYesNoDialogDialog({
      data: {
        title: 'Do you want to save your changes?',
      }
    });
  }

  public openCancelDialog(): Promise<boolean> {
    return this.openYesNoDialogDialog({
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

  public openAndThen<T, D = any, R = any>(dialog: ComponentType<T> | TemplateRef<T>, config?: MatDialogConfig<D>): Promise<R | undefined> {
    return this.dialog.open(dialog, config).afterClosed().toPromise();
  }

  private openYesNoDialogDialog(config: MatDialogConfig): Promise<boolean> {
    const defaultConfig = {
      data: {
        yesText: 'Yes',
        yesIcon: 'check_circle',
        noText: 'No',
        noIcon: 'cancel'
      }
    };
    Object.assign(defaultConfig.data, config.data);
    return this.openAndThen(YesNoDialogComponent, defaultConfig);
  }
}
