import {Component, OnInit} from '@angular/core';
import {MatDialogRef} from "@angular/material";

@Component({
  selector: 'app-move-dialog',
  templateUrl: './move-dialog.component.html',
  styleUrls: ['./move-dialog.component.css']
})
export class MoveDialogComponent {

  selectedSource: any;
  destinationsChoiceDisabled: boolean = false;
  queues: any[] = [];

  constructor(public dialogRef: MatDialogRef<MoveDialogComponent>) {
  }

  canOk(): boolean {
    return !!(this.selectedSource);
  }

}
