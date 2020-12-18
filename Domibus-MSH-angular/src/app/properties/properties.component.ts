import {AfterViewChecked, AfterViewInit, ChangeDetectorRef, Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {AlertService} from '../common/alert/alert.service';
import {PropertiesService, PropertyListModel, PropertyModel} from './support/properties.service';
import {SecurityService} from '../security/security.service';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/mixins/base-list.component';
import {ServerPageableListMixin} from '../common/mixins/pageable-list.mixin';
import FilterableListMixin from '../common/mixins/filterable-list.mixin';
import {HttpClient} from '@angular/common/http';
import {ApplicationContextService} from '../common/application-context.service';
import {ComponentName} from '../common/component-name-decorator';
import {MAT_CHECKBOX_CLICK_ACTION} from '@angular/material/checkbox';
import {SessionExpiredDialogComponent} from '../security/session-expired-dialog/session-expired-dialog.component';
import {DialogsService} from '../common/dialogs/dialogs.service';
import {AddNestedPropertyDialogComponent} from './support/add-nested-property-dialog/add-nested-property-dialog.component';
import {ServerSortableListMixin} from '../common/mixins/sortable-list.mixin';

@Component({
  moduleId: module.id,
  templateUrl: 'properties.component.html',
  styleUrls: ['properties.component.css'],
  providers: [PropertiesService, {provide: MAT_CHECKBOX_CLICK_ACTION, useValue: 'check'}]
})
@ComponentName('Domibus Properties')
export class PropertiesComponent extends mix(BaseListComponent)
  .with(FilterableListMixin, ServerPageableListMixin, ServerSortableListMixin)
  implements OnInit, AfterViewInit, AfterViewChecked {

  showGlobalPropertiesControl: boolean;

  @ViewChild('propertyValueTpl', {static: false}) propertyValueTpl: TemplateRef<any>;

  constructor(private applicationService: ApplicationContextService, private http: HttpClient, private propertiesService: PropertiesService,
              private alertService: AlertService, private securityService: SecurityService, private changeDetector: ChangeDetectorRef,
              private dialogsService: DialogsService) {
    super();
  }

  async ngOnInit() {
    super.ngOnInit();

    super.filter = {propertyName: '', showDomain: true, type: null, module: null, value: null, isWritable: true};
    this.showGlobalPropertiesControl = this.securityService.isCurrentUserSuperAdmin();

    this.propertiesService.loadPropertyTypes();
    this.filterData();
  }

  protected get GETUrl(): string {
    return PropertiesService.PROPERTIES_URL;
  }

  ngAfterViewInit() {
    this.columnPicker.allColumns = [
      {
        name: 'Property Name',
        prop: 'name',
        showInitially: true,
      },
      {
        name: 'Type',
        prop: 'type',
        showInitially: true,
        width: 25
      },
      {
        name: 'Description',
        prop: 'description',
        width: 25,
        sortable: false
      },
      {
        name: 'Module',
        prop: 'module',
        width: 25
      },
      {
        name: 'Section',
        prop: 'section',
        width: 25,
        sortable: false
      },
      {
        name: 'Usage',
        prop: 'usageText',
        showInitially: true,
        width: 25
      },
      {
        name: 'With Fallback',
        prop: 'withFallback',
        width: 25,
        sortable: false
      },
      {
        name: 'Is Writable',
        prop: 'writable',
        width: 25,
        sortable: false
      },
      {
        name: 'Is Encrypted',
        prop: 'encrypted',
        width: 25,
        sortable: false
      },
      {
        name: 'Is Composable',
        prop: 'composable',
        width: 25,
        sortable: false
      },
      {
        cellTemplate: this.propertyValueTpl,
        name: 'Property Value',
        prop: 'value',
        showInitially: true,
        width: 250,
        sortable: false
      },

    ];

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => col.showInitially);
  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }

  public setServerResults(result: PropertyListModel) {
    super.count = result.count;
    super.rows = result.items;
  }

  onPropertyValueFocus(row) {
    row.currentValueSet = true;
    row.currentValue = row.value;
    this.alertService.clearAlert();
  }

  onPropertyValueBlur(row) {
    setTimeout(() => this.revertProperty(row), 1500);
  }

  canUpdate(row): boolean {
    if (row && !row.currentValueSet) {
      return false;
    }
    return row && row.currentValue != row.value;
  }

  private async updateProperty(row) {
    try {
      row.oldValue = row.currentValue;
      row.currentValue = row.value;
      await this.propertiesService.updateProperty(row, this.filter.showDomain);
      this.alertService.success('Successfully updated the property ' + row.name);
    } catch (ex) {
      row.currentValue = row.oldValue;
      this.revertProperty(row);
      if (!ex.handled) {
        this.alertService.exception('Could not update property: ', ex);
      }
    }
  }

  private revertProperty(row) {
    row.value = row.currentValue;
  }

  get csvUrl(): string {
    return PropertiesService.PROPERTIES_URL + '/csv' + '?' + this.createAndSetParameters();
  }

  canWriteProperty(row) {
    return row.writable && !row.composable;
  }

  async addNewNestedProperty(property: PropertyModel) {
    const data = await this.dialogsService.openAndThen(AddNestedPropertyDialogComponent, {data: property});
    if (!data) {
      return;
    }
    let propertyName = property.name + '.' + data.propertySuffix;
    try {
      if (await this.propertyExists(propertyName)) {
        this.alertService.error(`There is already a property with the name ${propertyName}. Please update it instead if this is the intention`);
        return;
      }

      const newProp: PropertyModel = JSON.parse(JSON.stringify(property));
      newProp.name = propertyName;
      newProp.value = data.propertyValue;
      newProp.composable = false;
      await this.propertiesService.updateProperty(newProp, this.filter.showDomain);

      super.rows = [...this.rows, newProp];
      super.count = this.rows.length;
    } catch (e) {
      this.alertService.exception(`Error trying to add a property with the name ${propertyName}.`, e);
    }
  }

  private async propertyExists(propertyName: string): Promise<boolean> {
    try {
      const existing = await this.propertiesService.getProperty(propertyName);
      return existing != null;
    } catch (ex) {
      return false;
    }
  }
}
