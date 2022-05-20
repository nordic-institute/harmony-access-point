import {ChangeDetectorRef, Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {RoutingCriteriaEntry} from '../support/routingcriteriaentry';
import {BackendFilterEntry} from '../support/backendfilterentry';
import {EditPopupBaseComponent} from '../../common/edit-popup-base.component';

const NEW_MODE = 'New Message Filter';
const EDIT_MODE = 'Message Filter Edit';
const MAX_LENGTH = 255;

/**
 * @author Tiago Miguel, Ion Perpegel
 * @since 3.3
 *
 * In charge of creating and updating message filters
 */
@Component({
  selector: 'editmessagefilter-form',
  templateUrl: 'editmessagefilter-form.component.html',
  styleUrls: ['editmessagefilter-form.component.css'],
})
export class EditMessageFilterComponent extends EditPopupBaseComponent {

  formTitle: string;
  textMaxLength = MAX_LENGTH;

  backendFilterNames: Array<String> = [];

  entity: BackendFilterEntry;
  criteria: any;

  partyPattern = '[a-zA-Z0-9_:-]+:[a-zA-Z0-9_:-]+';
  partyPatternMessage = 'You should follow the rule: ' + this.partyPattern;

  actionPattern = '[a-zA-Z0-9_:-]+';
  actionPatternMessage = 'You should follow the rule: ' + this.actionPattern;

  servicePattern = '[a-zA-Z0-9_:\\\\./-]+:[a-zA-Z0-9_:-]+';
  servicePatternMessage = 'You should follow the rule: ' + this.servicePattern;

  constructor(public dialogRef: MatDialogRef<EditMessageFilterComponent>, @Inject(MAT_DIALOG_DATA) public data: any,
              private cdr: ChangeDetectorRef) {
    super(dialogRef, data);

    this.backendFilterNames = data.backendFilterNames;

    this.entity = this.data.entity;
    this.extractCriteria();

    this.formTitle = this.entity.persisted ? EDIT_MODE : NEW_MODE;
  }

  ngAfterViewInit() {
    this.cdr.detectChanges();
  }

  private extractCriteria() {
    this.criteria = BackendFilterEntry.routingCriteriaNames.reduce((map, key) => {
      map[key] = this.entity[key] ? this.entity[key].expression : null;
      return map;
    }, {});
  }

  onSubmitForm() {
    this.updateRoutingCriteria();
    this.entity.routingCriterias = BackendFilterEntry.routingCriteriaNames.map(name => this.entity[name]).filter(el => el != null);

    console.log('onsubmit ', this.entity.routingCriterias)
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

  isFormDisabled() {
    if (!this.editForm) {
      return true;
    }
    return this.editForm.invalid || (!this.editForm.dirty && this.entity.persisted);
  }
}
