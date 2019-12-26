import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {RoutingCriteriaEntry} from '../routingcriteriaentry';
import {BackendFilterEntry} from '../backendfilterentry';

let NEW_MODE = 'New Message Filter';
let EDIT_MODE = 'Message Filter Edit';
let MAX_LENGTH = 255;

@Component({
  selector: 'editmessagefilter-form',
  templateUrl: 'editmessagefilter-form.component.html',
  styleUrls: ['editmessagefilter-form.component.css'],
})
export class EditMessageFilterComponent {

  formTitle: string = EDIT_MODE;
  textMaxLength = MAX_LENGTH;

  backendFilterNames: Array<String> = [];

  entity: BackendFilterEntry;
  criteria: any;

  constructor(public dialogRef: MatDialogRef<EditMessageFilterComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {

    this.backendFilterNames = data.backendFilterNames;

    this.entity = this.data.entity;
    this.extractCriteria();

    // if (!(data.edit)) {
    //   this.formTitle = NEW_MODE;
    //   this.backendFilterNames = data.backendFilterNames;
    // this.plugin = this.backendFilterNames[0].toString();
    // this.from = '';
    // this.to = '';
    // this.action = '';
    // this.service = '';
    // } else {
    // let backEntry = new BackendFilterEntry(
    //   this.data.edit.entityId,
    //   this.data.edit.index,
    //   this.data.edit.backendName,
    //   this.data.edit.routingCriterias,
    //   this.data.edit.persisted);
    // this.backendFilterNames = data.backendFilterNames;
    // this.plugin = backEntry.backendName;
    // this.from = !(backEntry.from) ? '' : backEntry.from.expression;
    // this.to = !(backEntry.to) ? '' : backEntry.to.expression;
    // this.action = !(backEntry.action) ? '' : backEntry.action.expression;
    // this.service = !(backEntry.service) ? '' : backEntry.service.expression;
    // }
  }

  private extractCriteria() {
    this.criteria = BackendFilterEntry.routingCriteriaNames.reduce((map, key) => {
      map[key] = this.entity[key] ? this.entity[key].expression : null;
      return map;
    }, {});
  }

  submitForm() {
    this.updateRoutingCriteria();
    this.entity.routingCriterias = BackendFilterEntry.routingCriteriaNames.map(name => this.entity[name]).filter(el => el != null);

    this.dialogRef.close(true);
  }

  private updateRoutingCriteria() {
    BackendFilterEntry.routingCriteriaNames.forEach(name => {
      if (this.criteria[name]) {
        if (this.entity[name]) { //update
          this.entity[name].expression = this.criteria[name];
        } else { //add
          this.entity[name] = new RoutingCriteriaEntry(null, name, this.criteria[name]);
        }
      } else { //delete if exists
        delete this.entity[name];
      }
    });
  }

}
