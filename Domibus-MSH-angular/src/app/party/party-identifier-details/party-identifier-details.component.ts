import {Component, OnInit, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {EditPopupBaseComponent} from '../../common/edit-popup-base.component';

@Component({
  selector: 'app-party-identifier-details',
  templateUrl: './party-identifier-details.component.html',
  styleUrls: ['./party-identifier-details.component.css']
})
export class PartyIdentifierDetailsComponent extends EditPopupBaseComponent implements OnInit {
  public partyIdTypePattern = 'urn:oasis:names:tc:ebcore:partyid\\-type:[a-zA-Z0-9_:-]+';
  public partyIdTypeMessage = 'You should follow the rule: urn:oasis:names:tc:ebcore:partyid-type:[....]';

  partyIdentifier: any;

  constructor(public dialogRef: MatDialogRef<PartyIdentifierDetailsComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {
    super(dialogRef, data);

    this.partyIdentifier = data.edit;
  }

  ngOnInit() {
  }

}
