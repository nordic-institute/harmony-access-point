import {AfterViewChecked, AfterViewInit, ChangeDetectorRef, Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {AlertService} from '../common/alert/alert.service';
import {PropertiesService, PropertyListModel} from './support/properties.service';
import {SecurityService} from '../security/security.service';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/mixins/base-list.component';
import {ServerPageableListMixin} from '../common/mixins/pageable-list.mixin';
import FilterableListMixin from '../common/mixins/filterable-list.mixin';
import {HttpClient} from '@angular/common/http';
import {ApplicationContextService} from '../common/application-context.service';

@Component({
  moduleId: module.id,
  templateUrl: 'properties.component.html',
  styleUrls: ['properties.component.css'],
  providers: [PropertiesService]
})

export class PropertiesComponent extends mix(BaseListComponent)
  .with(FilterableListMixin, ServerPageableListMixin)
  implements OnInit, AfterViewInit, AfterViewChecked {

  showGlobalPropertiesControl: boolean;

  @ViewChild('propertyUsageTpl', {static: false}) propertyUsageTpl: TemplateRef<any>;
  @ViewChild('propertyValueTpl', {static: false}) propertyValueTpl: TemplateRef<any>;

  constructor(private applicationService: ApplicationContextService, private http: HttpClient, private propertiesService: PropertiesService,
              private alertService: AlertService, private securityService: SecurityService, private changeDetector: ChangeDetectorRef) {
    super();
  }

  async ngOnInit() {
    super.ngOnInit();

    super.filter = {propertyName: '', showDomain: true};
    this.showGlobalPropertiesControl = this.securityService.isCurrentUserSuperAdmin();

    this.propertiesService.loadPropertyTypes();
    this.filterData();
  }

  public get name(): string {
    return 'Domibus Properties';
  }

  protected get GETUrl(): string {
    return PropertiesService.PROPERTIES_URL;
  }

  ngAfterViewInit() {
    this.columnPicker.allColumns = [
      {
        name: 'Property Name',
        prop: 'name',
        showInitially: true
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
        width: 25
      },
      {
        name: 'Module',
        prop: 'module',
        width: 25
      },
      {
        name: 'Section',
        prop: 'section',
        width: 25
      },
      {
        cellTemplate: this.propertyUsageTpl,
        name: 'Usage',
        showInitially: true,
        width: 25
      },
      {
        name: 'With Fallback',
        prop: 'withFallback',
        width: 25
      },
      {
        name: 'Is Writable',
        prop: 'writable',
        width: 25
      },
      {
        name: 'Is Encrypted',
        prop: 'encrypted',
        width: 25
      },
      {
        name: 'Is Composable',
        prop: 'composable',
        width: 25
      },
      {
        cellTemplate: this.propertyValueTpl,
        name: 'Property Value',
        showInitially: true,
        width: 155
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
}
