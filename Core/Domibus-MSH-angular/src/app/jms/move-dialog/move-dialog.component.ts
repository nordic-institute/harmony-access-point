import {Component, Inject} from '@angular/core';
import {MatDialogRef} from '@angular/material';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';

@Component({
  selector: 'app-move-dialog',
  templateUrl: './move-dialog.component.html',
  styleUrls: ['./move-dialog.component.css']
})
export class MoveDialogComponent {

  selectedSource: any;
  destinationsChoiceDisabled = false;
  queues: any[] = [];

  constructor(public dialogRef: MatDialogRef<MoveDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {
    this.setQueues(data.queues);
  }

  private setQueues(queues: any[]) {
    if (queues && queues.length > 0) {
      this.queues = queues;
      this.selectedSource = queues[0];
      if (queues.length === 1) {
        this.destinationsChoiceDisabled = true;
      }
    }
  }

  canOk(): boolean {
    return !!(this.selectedSource);
  }

}
