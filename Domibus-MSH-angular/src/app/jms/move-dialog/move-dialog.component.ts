import {Component} from '@angular/core';
import {MatDialogRef} from '@angular/material';

@Component({
  selector: 'app-move-dialog',
  templateUrl: './move-dialog.component.html',
  styleUrls: ['./move-dialog.component.css']
})
export class MoveDialogComponent {

  selectedSource: any;
  destinationsChoiceDisabled = false;
  queues: any[] = [];

  constructor(public dialogRef: MatDialogRef<MoveDialogComponent>) {
  }

  setQueues(queues: any[]) {
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
