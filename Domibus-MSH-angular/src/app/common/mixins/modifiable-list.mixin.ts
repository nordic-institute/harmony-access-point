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
  public isSaving: boolean;

  constructor(...args) {
    super(...args);
  }

  ngOnInit(): void {
    if (super.ngOnInit) {
      super.ngOnInit();
    }

    this.isChanged = false;
    this.isSaving = false;
  }

  public isDirty(): boolean {
    return this.isChanged;
  }

  public doSave(): Promise<any> {
    return undefined;
  }

  async save(): Promise<boolean> {
    if (this.isSaving) return;

    const save = await this.dialogsService.openSaveDialog();
    if (save) {
      this.isSaving = true;
      return await this.doSave().then(() => {
        this.isSaving = false;
        this.alertService.success(`The operation 'update ${this.name}' completed successfully.`);
        this.page();
        return true;
      }, err => {
        this.isSaving = false;
        this.alertService.exception(`The operation 'update ${this.name}' did not complet successfully!`, err);
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
    if (this.isSaving) return;

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
