import {
  AfterViewChecked,
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  OnInit,
  TemplateRef,
  ViewChild
} from '@angular/core';
import {AlertService} from '../common/alert/alert.service';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/mixins/base-list.component';
import {ClientPageableListMixin} from '../common/mixins/pageable-list.mixin';
import {ComponentName} from '../common/component-name-decorator';
import {ClientSortableListMixin} from '../common/mixins/sortable-list.mixin';
import {DomainService} from '../security/domain.service';
import {Domain} from '../security/domain';

/**
 * @author Ion Perpegel
 * @since 5.0
 *
 * Domains management page
 */
@Component({
  moduleId: module.id,
  templateUrl: 'domains.component.html',
  styleUrls: ['domains.component.css'],
  providers: []
})
@ComponentName('Domains')
export class DomainsComponent extends mix(BaseListComponent).with(ClientPageableListMixin, ClientSortableListMixin)
  implements OnInit, AfterViewInit, AfterViewChecked {

  @ViewChild('rowActions', {static: false}) rowActions: TemplateRef<any>;
  @ViewChild('monitorStatus', {static: false}) statusTemplate: TemplateRef<any>;

  constructor(private alertService: AlertService, private domainService: DomainService, private changeDetector: ChangeDetectorRef) {
    super();
  }

  ngOnInit() {
    super.ngOnInit();

    this.loadServerData();
  }

  ngAfterViewInit() {
    this.initColumns();
  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }

  private async getDataAndSetResults() {
    let rows = await this.domainService.getAllDomains();
    super.rows = rows;
    super.count = this.rows.length;
  }

  private initColumns() {

    this.columnPicker.allColumns = [
      {
        name: 'Domain Code',
        prop: 'code',
        width: 10
      },
      {
        name: 'Domain Name',
        prop: 'name',
        width: 10
      },
      {
        cellTemplate: this.statusTemplate,
        name: 'Active',
        prop: 'active',
        width: 20,
        canAutoResize: true,
        sortable: false
      },
      // {
      //   cellTemplate: this.rowActions,
      //   name: 'Actions',
      //   prop: 'actions',
      //   width: 60,
      //   canAutoResize: true,
      //   sortable: false
      // }
    ];
    this.columnPicker.selectedColumns = this.columnPicker.allColumns;
  }

  async toggleActiveState(domain: Domain) {
    let active = domain.active;
    try {
      super.isLoading = true;
      await this.domainService.setActiveState(domain, active);
      this.alertService.success(`Successfully ${active ? 'added' : 'removed'} domain ${domain.name}`);
    } catch (err) {
      this.alertService.exception(`Error while ${active ? 'adding' : 'removing'} domain ${domain.name}`, err);
      setTimeout(() => domain.active = !active, 200);
    } finally {
      super.isLoading = false;
    }
  }

  refresh() {
    this.loadServerData();
  }
}
