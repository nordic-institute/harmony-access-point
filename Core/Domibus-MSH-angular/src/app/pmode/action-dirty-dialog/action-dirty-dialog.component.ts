import {Component, Inject, Input, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';

@Component({
  selector: 'app-action-dirty-dialog',
  templateUrl: './action-dirty-dialog.component.html',
  styleUrls: ['../support/pmode.component.css']
})
export class ActionDirtyDialogComponent implements OnInit {

  constructor(@Inject(MAT_DIALOG_DATA) public data: { actionTitle: string, actionName: string, actionIconName: string },
              public dialogRef: MatDialogRef<ActionDirtyDialogComponent>) {
  }

  ngOnInit() {
  }

}
