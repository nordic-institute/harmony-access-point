/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * A mixin for components that display a list of items that can be modified and saved
 */
import {Constructable} from '../base-list.component';
import {OnInit} from '@angular/core';
import {DirtyOperations} from '../dirty-operations';
import {RowLimiterBase} from '../row-limiter/row-limiter-base';
import {IPageableList} from './Ipageable-list';

let PageableListMixin = (superclass: Constructable) => class extends superclass
  implements IPageableList, OnInit {

  public offset: number;
  public rowLimiter: RowLimiterBase;

  constructor(...args) {
    super(...args);
  }

  public page() {
  }

  public ngOnInit(): void {
    if (super.ngOnInit) {
      super.ngOnInit();
    }

    this.offset = 0;
    this.rowLimiter = new RowLimiterBase();
  }

  public changePageSize(newPageLimit: number) {
    this.offset = 0;
    this.rowLimiter.pageSize = newPageLimit;
    this.page();
  }

  onPage(event) {
    this.offset = event.offset;
    this.page();
  }

  public canProceed(): Promise<boolean> {
    console.log('canProceed');
    if (super.hasMethod('isDirty') && this.isDirty()) {
      return this.dialogsService.openCancelDialog();
    }
    return Promise.resolve(true);
  }

  //we create this function like so to preserve the correct "this" when called from the row-limiter component context
  onPageSizeChanging = async (newPageLimit: number): Promise<boolean> => {
    const canChangePage = await this.canProceed();
    return !canChangePage;
  };

};

export default PageableListMixin;
