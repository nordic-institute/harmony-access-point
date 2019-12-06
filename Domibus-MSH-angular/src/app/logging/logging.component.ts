import {ChangeDetectorRef, Component, ElementRef, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {LoggingLevelResult} from './logginglevelresult';
import {AlertService} from '../common/alert/alert.service';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/mixins/base-list.component';
import FilterableListMixin from '../common/mixins/filterable-list.mixin';
import {ServerPageableListMixin} from '../common/mixins/pageable-list.mixin';
import SortableListMixin from '../common/mixins/sortable-list.mixin';

/**
 * @author Catalin Enache
 * @since 4.1
 */
@Component({
  moduleId: module.id,
  templateUrl: 'logging.component.html',
  providers: [],
})

export class LoggingComponent extends mix(BaseListComponent)
  .with(FilterableListMixin, ServerPageableListMixin, SortableListMixin) implements OnInit {

  static readonly LOGGING_URL: string = 'rest/logging/loglevel';
  static readonly RESET_LOGGING_URL: string = 'rest/logging/reset';

  @ViewChild('rowWithToggleTpl', {static: false}) rowWithToggleTpl: TemplateRef<any>;

  levels: Array<String>;
  isLoading: boolean = false;

  constructor(private elementRef: ElementRef, private http: HttpClient, private alertService: AlertService,
              private changeDetector: ChangeDetectorRef) {
    super();
  }

  ngOnInit() {
    super.ngOnInit();

    this.search();
  }

  ngAfterViewInit() {
    this.columnPicker.allColumns = [
      {
        name: 'Logger Name',
        prop: 'name'
      },
      {
        cellTemplate: this.rowWithToggleTpl,
        name: 'Logger Level'
      }
    ];
    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['Logger Name', 'Logger Level'].indexOf(col.name) != -1
    });
  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }

  createSearchParams(): HttpParams {
    let searchParams = new HttpParams();

    if (this.orderBy) {
      searchParams = searchParams.append('orderBy', this.orderBy);
    }
    if (this.asc != null) {
      searchParams = searchParams.append('asc', this.asc.toString());
    }

    if (this.filter.loggerName) {
      searchParams = searchParams.append('loggerName', this.activeFilter.loggerName);
    }
    if (this.filter.showClasses) {
      searchParams = searchParams.append('showClasses', this.activeFilter.showClasses);
    }

    return searchParams;
  }

  getLoggingEntries(): Observable<LoggingLevelResult> {
    let searchParams = this.createSearchParams();

    searchParams = searchParams.append('page', this.offset.toString());
    searchParams = searchParams.append('pageSize', this.rowLimiter.pageSize.toString());

    return this.http.get<LoggingLevelResult>(LoggingComponent.LOGGING_URL, {params: searchParams});
  }

  page() {
    this.isLoading = true;
    super.resetFilters();
    this.getLoggingEntries().subscribe((result: LoggingLevelResult) => {
      super.count = result.count;
      super.rows = result.loggingEntries;

      super.filter = result.filter;
      this.levels = result.levels;

      this.isLoading = false;
    }, (error: any) => {
      this.isLoading = false;
      this.alertService.exception('Error occurred:', error);
    });

  }

  onLevelChange(newLevel: string, row: any) {
    if (newLevel !== row.level) {
      this.alertService.clearAlert();
      this.http.post(LoggingComponent.LOGGING_URL, {
        name: row.name,
        level: newLevel,
      }, {headers: this.headers}).subscribe(
        () => {
          this.page();
        },
        error => {
          this.alertService.exception('An error occurred while setting logging level: ', error);
          this.isLoading = false;
        }
      );
    }
  }

  resetLogging() {
    this.http.post(LoggingComponent.RESET_LOGGING_URL, {}).subscribe(
      res => {
        this.alertService.success('Logging configuration was successfully reset.', false);
        this.page();
      },
      error => {
        this.alertService.exception('An error occurred while resetting logging: ', error);
        this.isLoading = false;
      }
    );
  }

  // search() {
  //   super.offset = 0;
  //   this.page();
  // }

}
