import {Constructable} from './base-list.component';
import {ChangeDetectorRef, OnInit} from '@angular/core';
import {DirtyOperations} from '../dirty-operations';
import {RowLimiterBase} from '../row-limiter/row-limiter-base';
import {IPageableList, PaginationType} from './Ipageable-list';
import {instanceOfFilterableList, instanceOfModifiableList, instanceOfPageableList} from './type.utils';
import {AlertsResult} from '../../alerts/alertsresult';

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * A mixin for components that display a list of items that are paged on server
 */
export let ServerPageableListMixin = (superclass: Constructable) => class extends PageableListMixin(superclass) {
  constructor(...args) {
    super(...args);
    super.type = PaginationType.Server;
  }
}

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * A mixin for components that display a list of items that are paged on client
 */
export let ClientPageableListMixin = (superclass: Constructable) => class extends PageableListMixin(superclass) {
  constructor(...args) {
    super(...args);
    super.type = PaginationType.Client;
  }
}

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * A mixin for components that display a list of items that are paged on server or client
 */
export let PageableListMixin = (superclass: Constructable) => class extends superclass
  implements IPageableList, OnInit {

  public type: PaginationType;
  public offset: number;
  public rowLimiter: RowLimiterBase;
  public isLoading: boolean;

  constructor(...args) {
    super(...args);

    this.offset = 0;
    this.rowLimiter = new RowLimiterBase();
  }

  async doLoadPage(): Promise<any> {
  }

  public ngOnInit(): void {
    if (super.ngOnInit) {
      super.ngOnInit();
    }
    this.isLoading = false;
  }

  public page() {
    this.isLoading = true;
    this.resetFilters();
    this.doLoadPage().then((result: AlertsResult) => {
      this.isLoading = false;
      if(instanceOfModifiableList(this)) {
        this.isChanged = false;
      }
    }, (error: any) => {
      this.isLoading = false;
      this.alertService.exception('Error occurred:', error);
    });
  }

  public changePageSize(newPageLimit: number) {
    this.offset = 0;
    this.rowLimiter.pageSize = newPageLimit;

    if (instanceOfFilterableList(this)) {
      this.resetFilters();
    }
    this.page();
  }

  public async onPage(event) {
    this.loadPage(event.offset);
  }

  public async resetPage() {
    return this.loadPage(0);
  }

  public async loadPage(offset: number) {
    const canChangePage = await this.canProceedToPageChange();
    if (canChangePage) {
      if (instanceOfFilterableList(this)) {
        this.resetFilters();
      }
      this.offset = offset;
      this.page();
    } else {
      //how to make grid show the correct page??
    }
    return canChangePage;
  }

  private canProceedToPageChange(): Promise<boolean> {
    if (this.type == PaginationType.Server) {
      if (instanceOfModifiableList(this) && this.isDirty()) {
        return this.dialogsService.openCancelDialog();
      }
    }
    return Promise.resolve(true);
  }

  //we create this function like so to preserve the correct "this" when called from the row-limiter component context
  onPageSizeChanging = async (newPageLimit: number): Promise<boolean> => {
    const canChangePage = await this.canProceedToPageChange();
    return !canChangePage;
  };

};

