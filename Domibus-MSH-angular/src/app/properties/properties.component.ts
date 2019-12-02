import {ChangeDetectorRef, Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {AlertService} from '../common/alert/alert.service';
import {PropertiesService} from './properties.service';
import {RowLimiterBase} from '../common/row-limiter/row-limiter-base';
import {SecurityService} from '../security/security.service';

@Component({
  moduleId: module.id,
  templateUrl: 'properties.component.html',
  styleUrls: ['properties.component.css'],
  providers: [PropertiesService]
})

export class PropertiesComponent implements OnInit {

  filter: { propertyName: string, showDomainProperties: boolean };
  showGlobalPropertiesControl: boolean;

  loading: boolean = false;
  rows = [];
  count: number = 0;
  offset: number = 0;
  rowLimiter: RowLimiterBase = new RowLimiterBase();

  @ViewChild('propertyValueTpl', {static: false}) propertyValueTpl: TemplateRef<any>;

  columns: any[] = [];

  constructor(private propertiesService: PropertiesService, private alertService: AlertService,
              private securityService: SecurityService, private changeDetector: ChangeDetectorRef) {
    this.filter = {propertyName: '', showDomainProperties: true};
  }

  async ngOnInit() {
    this.showGlobalPropertiesControl = this.securityService.isCurrentUserSuperAdmin();

    this.rows = [];

    this.page();
  }

  ngAfterViewInit() {
    this.columns = [
      {
        name: 'Property Name',
        prop: 'metadata.name'
      },
      {
        cellTemplate: this.propertyValueTpl,
        name: 'Property Value'
      }
    ];
  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }

  onPropertyNameChanged() {
    this.page();
  }

  onPage(event) {
    this.offset = event.offset;
    this.page();
  }

  onChangePageSize(newPageLimit: number) {
    this.offset = 0;
    this.rowLimiter.pageSize = newPageLimit;
    this.page();
  }

  onPropertyValueFocus(row) {
    row.currentValue = row.value;
  }

  onPropertyValueBlur(row) {
    setTimeout(() => this.revertProperty(row), 1500);
  }

  canUpdate(row): boolean {
    return row && row.currentValue && row.currentValue != row.value;
  }

  private async page() {
    this.loading = true;
    try {
      var result = await this.propertiesService.getProperties(this.filter.propertyName, this.filter.showDomainProperties,
        this.rowLimiter.pageSize, this.offset);
      this.count = result.count;
      this.rows = result.items;
    } catch (ex) {
      this.alertService.exception('Could not load properties ', ex, false);
    }
    this.loading = false;
  }

  refresh() {
    this.offset = 0;
    this.page();
  }

  private async updateProperty(row) {
    try {
      row.oldValue = row.currentValue;
      row.currentValue = row.value;
      await this.propertiesService.updateProperty(row.metadata.name, this.filter.showDomainProperties, row.value);
    } catch (ex) {
      row.currentValue = row.oldValue;
      this.revertProperty(row);
      if (!ex.handled)
        this.alertService.exception('Could not update property ', ex, false);
    }
  }

  private revertProperty(row) {
    row.value = row.currentValue;
  }

}
