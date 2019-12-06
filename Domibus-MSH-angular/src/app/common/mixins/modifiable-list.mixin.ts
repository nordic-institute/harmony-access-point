import {Constructable} from './base-list.component';
import {DirtyOperations} from '../dirty-operations';
import {IModifiableList} from './imodifiable-list';
import {OnInit} from '@angular/core';
import {instanceOfPageableList} from './type.utils';

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * A mixin for components that display a list of items that can be modified and saved
 */
let ModifiableListMixin = (superclass: Constructable) => class extends superclass
  implements IModifiableList, DirtyOperations, OnInit {

  public isChanged: boolean;

  constructor(...args) {
    super(...args);
  }

  ngOnInit(): void {
    if (super.ngOnInit) {
      super.ngOnInit();
    }

    this.isChanged = false;
  }

  public isDirty(): boolean {
    return undefined;
  }

  public doSave(): Promise<any> {
    return undefined;
  }

  async save(): Promise<boolean> {
    const save = await this.dialogsService.openSaveDialog();
    if (save) {
      return await this.doSave().then(() => {
        this.alertService.success('The operation \'update\' completed successfully.');
        this.page();
        return true;
      }, err => {
        this.alertService.exception('The operation \'update\' not completed successfully', err);
        this.page();
        return false;
      });
    } else {
      return false;
    }
  }

  public async saveIfNeeded(): Promise<boolean> {
    if (this.isDirty()) {
      return this.save();
    } else {
      return false;
    }
  }

  public async cancel() {
    const cancel = await this.dialogsService.openCancelDialog();
    if (cancel) {
      this.isChanged = false;
      if (instanceOfPageableList(this)) {
        this.page();
      }
    }
  }

};

export default ModifiableListMixin;
