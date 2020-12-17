/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * An interface for the modifiable list ( client or server)
 * */
export interface IModifiableList {
  isChanged: boolean;
  isSaving: boolean;

  isDirty(): boolean;

  save(): Promise<boolean>;

  doSave(): Promise<any>

  saveIfNeeded(): Promise<boolean>;

  cancel();

  edit(row?: any);

  delete(row?: any);

  canCancel(): boolean;
  canSave(): boolean;
  canDelete(): boolean;
  canEdit(): boolean;
  canAdd(): boolean;
}

