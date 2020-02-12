import {Constructable} from './base-list.component';
import {DirtyOperations} from '../dirty-operations';
import {IModifiableList} from './imodifiable-list';
import {OnInit} from '@angular/core';

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
    if (this.isSaving) {
      return false;
    }

    const save = await this.dialogsService.openSaveDialog();
    if (save) {
      this.isSaving = true;
      let saved: boolean;
      let operationName: string = 'update ' + (this.name || '').toLowerCase();
      try {
        let result = await this.doSave();
        if (result === false) {
          saved = false; // TODO do not expect false after client validation on save
        } else {
          if(result != null) {
            this.alertService.success(result);
          } else {
            this.alertService.success(`The operation '${operationName}' completed successfully.`);
          }
          saved = true;
        }
      } catch (err) {
        this.alertService.exception(`The operation '${operationName}' did not complete successfully.`, err);
        saved = false;
      }
      this.isSaving = false;
      return saved;
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
      this.loadServerData();
    }
  }

  public edit(row?: any) {
  }

  public delete(row?: any) {
  }

  public add() {
  }

  canCancel(): boolean {
    return this.isChanged;
  }

  canSave(): boolean {
    return this.isChanged;
  }

  canDelete(): boolean {
    return !this.isChanged;
  }

  canEdit(): boolean {
    return !this.isSaving;
  }

  canAdd(): boolean {
    return !this.isSaving;
  }

  isBusy(): boolean {
    return super.isBusy() || this.isSaving;
  }
};

export default ModifiableListMixin;
