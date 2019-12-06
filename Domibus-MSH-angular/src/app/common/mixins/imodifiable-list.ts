/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * An interface for the modifiable list ( client or server)
 * */
export interface IModifiableList {
  isDirty(): boolean;

  save(): Promise<boolean>;

  saveIfNeeded(): Promise<boolean>;
}

