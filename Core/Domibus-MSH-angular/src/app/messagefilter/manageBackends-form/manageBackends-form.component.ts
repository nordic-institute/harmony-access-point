import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {EditPopupBaseComponent} from '../../common/edit-popup-base.component';
import {PropertiesService, PropertyModel} from '../../properties/support/properties.service';
import {AlertService} from '../../common/alert/alert.service';

/**
 * @author Ion Perpegel
 * @since 5.1
 *
 * In charge of enabling and disabling backend connectors
 */
@Component({
  selector: 'manageBackends-form',
  templateUrl: 'manageBackends-form.component.html',
  styleUrls: ['manageBackends-form.component.css'],
})
export class ManageBackendsComponent extends EditPopupBaseComponent {

  formTitle: string;

  backendConnectors: Array<{ name: string, active: boolean, enabledPropertyName: string }> = [];
  selected: any[];

  constructor(public dialogRef: MatDialogRef<ManageBackendsComponent>, @Inject(MAT_DIALOG_DATA) public data: any,
              private propertiesService: PropertiesService, private alertService: AlertService) {
    super(dialogRef, data);

    this.selected = [];
    this.backendConnectors = data.backendConnectors;
    this.formTitle = 'Manage Plugins';
  }

  async toggleActive(row: { name: string, active: boolean, enabledPropertyName: string }) {
    let newValue = row.active;
    let newValueText = `${(newValue ? 'enabled' : 'disabled')}`;

    try {
      let propName = row.enabledPropertyName;
      if (!propName) {
        throw new Error(`Backend connector doesn't have a property for enabled!`);
      }
      let prop: PropertyModel = await this.propertiesService.getProperty(propName);
      prop.value = newValue + '';
      await this.propertiesService.updateProperty(prop);

      row.active = newValue;
      this.alertService.success(`Backend connector <b>${row.name}</b> was successfully ${newValueText}.`);
    } catch (err) {
      row.active = !newValue;
      this.alertService.exception(`Backend connector <b>${row.name}</b> could not be ${newValueText}.`, err);
    }
  }

  onSubmitForm() {
  }

}
