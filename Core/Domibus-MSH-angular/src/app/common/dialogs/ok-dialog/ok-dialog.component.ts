import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {OKDialogData, YesNoDialogData} from '../DialogData';

@Component({
  selector: 'app-ok-dialog',
  templateUrl: './ok-dialog.component.html',
  styleUrls: ['./ok-dialog.component.css']
})
export class OkDialogComponent {

  constructor(@Inject(MAT_DIALOG_DATA) public data: OKDialogData,
              public dialogRef: MatDialogRef<OkDialogComponent>) {
  }

}
