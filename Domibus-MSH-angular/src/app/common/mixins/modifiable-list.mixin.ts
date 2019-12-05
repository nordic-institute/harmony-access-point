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

};

export default ModifiableListMixin;
