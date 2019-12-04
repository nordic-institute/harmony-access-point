/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * A mixin for components that display a list of items that can be modified and saved
 */
import {Constructable} from '../base-list.component';
import {OnInit} from '@angular/core';
import {DirtyOperations} from '../dirty-operations';

let ModifiableListMixin = (superclass: Constructable) => class extends superclass
  implements DirtyOperations {

  constructor(...args) {
    super(...args);
  }

  public isDirty(): boolean {
    return undefined;
  }

  async save(): Promise<boolean> {
    return undefined;
  }

  public async saveIfNeeded(): Promise<boolean> {
    if (this.isDirty()) {
      return this.save();
    } else {
      return false;
    }
  }

  protected canProceed(): Promise<boolean> {
    //todo: we also need to check if the pagination is done on the server
    if (!this.isDirty()) {
      return Promise.resolve(true);
    }

    return this.dialogsService.openCancelDialog();
  }

  //todo:even better: this code to be moved on the future paginableList mixin and check for dirty to decide if can proceed
  //we create this function like so to preserve the correct "this" when called from the row-limiter component context
  onPageSizeChanging = async (newPageLimit: number): Promise<boolean> => {
    const canChangePage = await this.canProceed();
    return !canChangePage;
  };

};

export default ModifiableListMixin;
