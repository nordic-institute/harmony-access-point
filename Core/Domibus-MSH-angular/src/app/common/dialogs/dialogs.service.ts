import {Injectable, TemplateRef} from '@angular/core';
import {ComponentType} from '@angular/cdk/overlay';
import { MatDialog, MatDialogConfig, MatDialogRef} from '@angular/material/dialog';
import {YesNoDialogComponent} from './yes-no-dialog/yes-no-dialog.component';
import {OkDialogComponent} from './ok-dialog/ok-dialog.component';

@Injectable()
export class DialogsService {
  private activeDialog: MatDialogRef<any, any>;

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

  public openResendAllDialog(): Promise<boolean> {
    return this.openYesNoDialogDialog({
      data: {
        message: 'Messages that match the filter applied on this page will be resent. Additionally, any new failed messages generated since you opened this page will also be resent.',
        title: 'Do you want to resend all the failed messages?\n Click on "Resend All" to proceed or "Cancel" to abort.',
        yesText: 'Resend All',
        yesIcon: 'send',
        noText: 'Cancel'
      }
    });
  }

  public openResendSelectedDialog(): Promise<boolean> {
    return this.openYesNoDialogDialog({
      data: {
        title: 'Do you want to resend the selected failed messages?\n Click on "Resend Selected" to proceed or "Cancel" to abort.',
        yesText: 'Resend Selected',
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

  public openYesNoDialog(question: string): Promise<boolean> {
    return this.openYesNoDialogDialog({
      data: {
        title: question,
      }
    });
  }

  public open<T, D = any, R = any>(dialog: ComponentType<T> | TemplateRef<T>, config?: MatDialogConfig<D>): MatDialogRef<T, R> {
    this.activeDialog = this.dialog.open(dialog, config);
    return this.activeDialog;
  }

  public openAndThen<T, D = any, R = any>(dialog: ComponentType<T> | TemplateRef<T>, config?: MatDialogConfig<D>): Promise<R | undefined> {
    this.activeDialog = this.dialog.open(dialog, config);
    // console.log('setting active dialog to=', this.activeDialog);
    return this.activeDialog.afterClosed().toPromise();
  }

   openYesNoDialogDialog(config: MatDialogConfig): Promise<boolean> {
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

  public openOkDialog(title: string, message: string): Promise<boolean> {
    return this.openOkDialogDialog({
      data: {
        title: title,
        message: message
      }
    });
  }

  public openOkDialogDialog(config: MatDialogConfig): Promise<boolean> {
    const defaultConfig = {
      data: {
        okText: 'OK',
        okIcon: 'check_circle',
      }
    };
    Object.assign(defaultConfig.data, config.data);
    return this.openAndThen(OkDialogComponent, defaultConfig);
  }

  closeActive() {
    if (this.activeDialog) {
      console.log('closing dialog=', this.activeDialog)
      this.activeDialog.close();
    }
  }

}
