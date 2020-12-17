import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';

@Component({
  selector: 'app-pmode-upload',
  templateUrl: './pmode-view.component.html',
  styleUrls: ['../../support/pmode.component.css']
})
export class PmodeViewComponent implements OnInit {

  public pMode: { metadata: any, content: string };
  public pModeType: string;
  public display = false;

  @ViewChild('pmode_view_content', {static: false})
  private pmode_view_content;

  constructor(@Inject(MAT_DIALOG_DATA) public data: { metadata: any, content: string },
              public dialogRef: MatDialogRef<PmodeViewComponent>) {
  }

  ngOnInit() {
    this.pMode = this.data;
    this.pModeType = this.pMode.metadata.current ? 'Current' : 'Archive';
    console.log(this.pmode_view_content);
    setTimeout(() => {
      this.display = true;
    }, 500);
  }

  ok() {
    this.dialogRef.close();
  };
}
