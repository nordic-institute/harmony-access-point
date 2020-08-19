import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {AuditService} from './audit.service';
import {UserService} from '../user/user.service';
import {AlertService} from '../common/alert/alert.service';
import {AuditCriteria, AuditResponseRo} from './audit';
import {RowLimiterBase} from '../common/row-limiter/row-limiter-base';
import {ColumnPickerBase} from '../common/column-picker/column-picker-base';
import {Observable} from 'rxjs/Observable';
import {AlertComponent} from '../common/alert/alert.component';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/base-list.component';
import FilterableListMixin from '../common/mixins/filterable-list.mixin';

/**
 * @author Thomas Dussart
 * @since 4.0
 *
 * In charge of retrieving audit information from the backend.
 */

@Component({
  selector: 'app-audit',
  providers: [AuditService, UserService],
  templateUrl: './audit.component.html',
  styleUrls: ['./audit.component.css']
})
export class AuditComponent extends mix(BaseListComponent).with(FilterableListMixin) implements OnInit {

  @ViewChild('rowWithDateFormatTpl') rowWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('rawTextTpl') public rawTextTpl: TemplateRef<any>;

// --- Search components binding ---
  existingAuditTargets = [];
  existingUsers = [];
  existingActions = [];

  timestampFromMaxDate: Date;
  timestampToMinDate: Date;
  timestampToMaxDate: Date;

  loading: boolean = false;

// --- hide/show binding ---
  advancedSearch: boolean;

// --- Table binding ---
  rows = [];
  rowLimiter: RowLimiterBase = new RowLimiterBase();
  columnPicker: ColumnPickerBase = new ColumnPickerBase();
  offset: number = 0;
  count: number = 0;

  constructor(private auditService: AuditService, private userService: UserService, private alertService: AlertService) {
    super();
  }

  ngOnInit() {
    super.ngOnInit();

// --- lets init the component's data ---
    this.existingUsers = [];
    const userObservable = this.userService.getUserNames();
    userObservable.subscribe((userName: string) => this.existingUsers.push(userName));

    this.existingActions = [];
    const actionObservable = this.auditService.listActions();
    actionObservable.subscribe((action: string) => this.existingActions.push(action));

    this.existingAuditTargets = [];
    const existingTargets = this.auditService.listTargetTypes();
    existingTargets.subscribe((target: string) => this.existingAuditTargets.push(target));

    this.timestampFromMaxDate = new Date();
    this.timestampToMinDate = null;
    this.timestampToMaxDate = new Date();

// --- lets init the table columns ---
    this.initColumns();

// --- lets count the records and fill the table.---
    this.searchAndCount();
  }

  searchAndCount() {
    this.setActiveFilter();

    this.loading = true;
    this.offset = 0;
    const auditCriteria: AuditCriteria = this.buildCriteria();
    const auditLogsObservable = this.auditService.listAuditLogs(auditCriteria);
    const auditCountObservable: Observable<number> = this.auditService.countAuditLogs(auditCriteria);
    auditLogsObservable.subscribe((response: AuditResponseRo[]) => {
        this.rows = response;
        this.loading = false;
      },
      error => {
        this.alertService.exception('Could not load audits: ', error);
        this.loading = false;
      },
      // on complete of auditLogsObservable Observable, we load the count
      // TODO: load this in parallel and merge the stream at the end.
      () => auditCountObservable.subscribe(auditCount => this.count = auditCount,
        error => {
          this.alertService.exception('Could not count audits: ', error);
          this.loading = false;
        })
    );
  }

  toggleAdvancedSearch() {
    this.advancedSearch = !this.advancedSearch;
    return false; // to prevent default navigation
  }

  searchAuditLog() {
    this.loading = true;
    const auditCriteria: AuditCriteria = this.buildCriteria();
    const auditLogsObservable = this.auditService.listAuditLogs(auditCriteria);
    auditLogsObservable.subscribe((response: AuditResponseRo[]) => {
        this.rows = response;
        this.loading = false;
      },
      error => {
        this.alertService.exception('Could not load audits: ', error);
        this.loading = false;
      });
  }

  onPage(event) {
    this.resetFilters();

    this.offset = event.offset;
    this.searchAuditLog();
  }

  buildCriteria(): AuditCriteria {
    const auditCriteria: AuditCriteria = new AuditCriteria();

    auditCriteria.auditTargetName = this.activeFilter.auditTarget;
    auditCriteria.user = this.activeFilter.users;
    auditCriteria.action = this.activeFilter.actions;
    auditCriteria.from = this.activeFilter.from;
    auditCriteria.to = this.activeFilter.to;

    auditCriteria.start = this.offset * this.rowLimiter.pageSize;
    auditCriteria.max = this.rowLimiter.pageSize;

    return auditCriteria;
  }

  changePageSize(newPageLimit: number) {
    this.resetFilters();

    this.offset = 0;
    this.rowLimiter.pageSize = newPageLimit;
    this.searchAuditLog();
  }

  initColumns() {
    this.columnPicker.allColumns = [
      {
        name: 'Table',
        prop: 'auditTargetName',
        width: 20,
        sortable: false
      },
      {
        name: 'User',
        prop: 'user',
        width: 20,
        sortable: false
      },
      {
        name: 'Action',
        prop: 'action',
        width: 20,
        sortable: false
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Changed',
        prop: 'changed',
        width: 80,
        sortable: false
      },
      {
        name: 'Id',
        prop: 'id',
        cellTemplate: this.rawTextTpl,
        width: 300,
        sortable: false
      }
    ];
    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['Table', 'User', 'Action', 'Changed', 'Id'].indexOf(col.name) != -1
    })
  }

  saveAsCSV() {
    if (this.rows.length > AlertComponent.MAX_COUNT_CSV) {
      this.alertService.error(AlertComponent.CSV_ERROR_MESSAGE);
      return;
    }
    super.resetFilters();
    const auditCriteria: AuditCriteria = this.buildCriteria();
    this.auditService.saveAsCsv(auditCriteria);
  }

  onTimestampFromChange(event) {
    this.timestampToMinDate = event.value;
  }

  onTimestampToChange(event) {
    this.timestampFromMaxDate = event.value;
  }
}
