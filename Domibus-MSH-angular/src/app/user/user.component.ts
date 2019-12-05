import {ChangeDetectorRef, Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {UserResponseRO, UserState} from './user';
import {UserSearchCriteria, UserService} from './user.service';
import {MAT_CHECKBOX_CLICK_ACTION, MatDialog, MatDialogRef} from '@angular/material';
import {UserValidatorService} from 'app/user/uservalidator.service';
import {AlertService} from '../common/alert/alert.service';
import {EditUserComponent} from 'app/user/edituser-form/edituser-form.component';
import {isNullOrUndefined} from 'util';
import {HttpClient} from '@angular/common/http';
import {DirtyOperations} from '../common/dirty-operations';
import {ColumnPickerBase} from '../common/column-picker/column-picker-base';
import {RowLimiterBase} from '../common/row-limiter/row-limiter-base';
import {SecurityService} from '../security/security.service';
import {DomainService} from '../security/domain.service';
import {Domain} from '../security/domain';
import {DialogsService} from '../common/dialogs/dialogs.service';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/base-list.component';
import FilterableListMixin from '../common/mixins/filterable-list.mixin';
import ModifiableListMixin from '../common/mixins/modifiable-list.mixin';
import {ClientPageableListMixin} from '../common/mixins/pageable-list.mixin';

@Component({
  moduleId: module.id,
  templateUrl: 'user.component.html',
  styleUrls: ['./user.component.css'],
  providers: [
    {provide: MAT_CHECKBOX_CLICK_ACTION, useValue: 'check'}
  ]
})

export class UserComponent extends mix(BaseListComponent)
  .with(FilterableListMixin, ModifiableListMixin, ClientPageableListMixin)
  implements OnInit, DirtyOperations {

  static readonly USER_URL: string = 'rest/user';
  static readonly USER_USERS_URL: string = UserComponent.USER_URL + '/users';
  static readonly USER_CSV_URL: string = UserComponent.USER_URL + '/csv';

  @ViewChild('passwordTpl', {static: false}) passwordTpl: TemplateRef<any>;
  @ViewChild('editableTpl', {static: false}) editableTpl: TemplateRef<any>;
  @ViewChild('checkBoxTpl', {static: false}) checkBoxTpl: TemplateRef<any>;
  @ViewChild('deletedTpl', {static: false}) deletedTpl: TemplateRef<any>;
  @ViewChild('rowActions', {static: false}) rowActions: TemplateRef<any>;

  // columnPicker: ColumnPickerBase = new ColumnPickerBase();
  // rowLimiter: RowLimiterBase = new RowLimiterBase();

  userRoles: Array<String>;
  domains: Domain[];
  domainsPromise: Promise<Domain[]>;
  currentDomain: Domain;

  selected: any[];

  enableCancel: boolean;
  enableSave: boolean;
  enableDelete: boolean;
  enableEdit: boolean;

  currentUser: UserResponseRO;

  editedUser: UserResponseRO;

  dirty: boolean;
  areRowsDeleted: boolean;

  deletedStatuses: any[];
  // offset: number;

  isBusy = false;

  constructor(private http: HttpClient,
              private userService: UserService,
              public dialog: MatDialog,
              private dialogsService: DialogsService,
              private userValidatorService: UserValidatorService,
              private alertService: AlertService,
              private securityService: SecurityService,
              private domainService: DomainService,
              private changeDetector: ChangeDetectorRef) {
    super();
  }

  async ngOnInit() {
    super.ngOnInit();

    this.isBusy = true;
    super.filter = new UserSearchCriteria();
    this.deletedStatuses = [null, true, false];

    // this.columnPicker = new ColumnPickerBase();
    // this.rowLimiter = new RowLimiterBase();

    super.rows = [];
    super.count = 0;
    this.userRoles = [];

    this.enableCancel = false;
    this.enableSave = false;
    this.enableDelete = false;
    this.enableEdit = false;
    this.currentUser = null;
    this.editedUser = null;

    this.selected = [];

    this.domainService.getCurrentDomain().subscribe((domain: Domain) => this.currentDomain = domain);

    this.getUsers();

    this.getUserRoles();

    this.dirty = false;
    this.areRowsDeleted = false;
  }

  async ngAfterViewInit() {
    this.columnPicker.allColumns = [
      {
        cellTemplate: this.editableTpl,
        name: 'Username',
        prop: 'userName',
        canAutoResize: true
      },
      {
        cellTemplate: this.editableTpl,
        name: 'Role',
        prop: 'roles',
        canAutoResize: true
      },
      {
        cellTemplate: this.editableTpl,
        name: 'Email',
        prop: 'email',
        canAutoResize: true
      },
      {
        cellTemplate: this.passwordTpl,
        name: 'Password',
        prop: 'password',
        canAutoResize: true,
        sortable: false,
        width: 25
      },
      {
        cellTemplate: this.checkBoxTpl,
        name: 'Active',
        canAutoResize: true,
        width: 25
      },
      {
        cellTemplate: this.deletedTpl,
        name: 'Deleted',
        canAutoResize: true,
        width: 25
      },
      {
        cellTemplate: this.rowActions,
        name: 'Actions',
        width: 60,
        canAutoResize: true,
        sortable: false
      }
    ];

    const showDomain = await this.userService.isDomainVisible();
    if (showDomain) {
      this.getUserDomains();

      this.columnPicker.allColumns.splice(2, 0,
        {
          cellTemplate: this.editableTpl,
          name: 'Domain',
          prop: 'domainName',
          canAutoResize: true
        });
    }

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['Username', 'Role', 'Domain', 'Active', 'Deleted', 'Actions'].indexOf(col.name) !== -1
    });
  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }

  async getUsers() {
    this.setActiveFilter();
    this.isBusy = true;
    try {
      let results = await this.userService.getUsers(this.activeFilter).toPromise();
      const showDomain = await this.userService.isDomainVisible();
      if (showDomain) {
        await this.getUserDomains();
        results.forEach(user => this.setDomainName(user));
      }
      super.rows = results;
      super.count = results.length;
    } catch (err) {
      this.alertService.exception('Could not load users ', err);
    }

    this.isBusy = false;
    this.dirty = false;
    this.areRowsDeleted = false;
  }

  private setDomainName(user) {
    const domains = this.domains;
    if (domains) {
      const domain = domains.find(d => d.code == user.domain);
      if (domain) {
        user.domainName = domain.name;
      }
    }
  }

  getUserRoles(): void {
    this.userService.getUserRoles().subscribe(userroles => this.userRoles = userroles);
  }

  async getUserDomains(): Promise<Domain[]> {
    if (this.domainsPromise) {
      return this.domainsPromise;
    }
    this.domainsPromise = this.domainService.getDomains();
    this.domains = await this.domainsPromise;
    return this.domains;
  }

  onSelect({selected}) {
    if (isNullOrUndefined(selected) || selected.length == 0) {
      // unselect
      this.enableDelete = false;
      this.enableEdit = false;

      return;
    }

    // select
    this.currentUser = this.selected[0];
    this.editedUser = this.currentUser;

    this.selected.splice(0, this.selected.length);
    this.selected.push(...selected);
    this.enableDelete = selected.length > 0 && !selected.every(el => el.deleted);
    this.enableEdit = selected.length == 1 && !selected[0].deleted;
  }

  private isLoggedInUserSelected(selected): boolean {
    let currentUser = this.securityService.getCurrentUser();
    for (let entry of selected) {
      if (currentUser && currentUser.username === entry.userName) {
        return true;
      }
    }
    return false;
  }

  buttonNew(): void {
    if (this.isBusy) return;

    this.setPage(this.getLastPage());

    this.editedUser = new UserResponseRO('', this.currentDomain, '', '', true, UserState[UserState.NEW], [], false, false);
    this.setIsDirty();
    const formRef: MatDialogRef<EditUserComponent> = this.dialog.open(EditUserComponent, {
      data: {
        edit: false,
        user: this.editedUser,
        userroles: this.userRoles,
        userdomains: this.domains
      }
    });
    formRef.afterClosed().subscribe(ok => {
      if (ok) {
        this.onSaveEditForm(formRef);
        super.rows = [...this.rows, this.editedUser];
        super.count = this.count + 1;
        this.currentUser = this.editedUser;
      } else {
        this.selected = [];
        this.enableEdit = false;
        this.enableDelete = false;
      }
      this.setIsDirty();
    });
  }

  buttonEdit() {
    if (this.currentUser && this.currentUser.deleted) {
      this.alertService.error('You cannot edit a deleted user.', false, 5000);
      return;
    }
    this.buttonEditAction(this.currentUser);
  }

  buttonEditAction(currentUser) {
    if (this.isBusy) return;

    const formRef: MatDialogRef<EditUserComponent> = this.dialog.open(EditUserComponent, {
      data: {
        edit: true,
        user: currentUser,
        userroles: this.userRoles,
        userdomains: this.domains
      }
    });
    formRef.afterClosed().subscribe(ok => {
      if (ok) {
        this.onSaveEditForm(formRef);
        this.setIsDirty();
      }
    });
  }

  private onSaveEditForm(formRef: MatDialogRef<EditUserComponent>) {
    const editForm = formRef.componentInstance;
    const user = this.editedUser;
    if (!user) return;

    user.userName = editForm.userName || user.userName; // only for add
    user.email = editForm.email;
    user.roles = editForm.role;
    user.domain = editForm.domain;
    this.setDomainName(user);
    user.password = editForm.password;
    user.active = editForm.active;

    if (editForm.userForm.dirty) {
      if (UserState[UserState.PERSISTED] === user.status) {
        user.status = UserState[UserState.UPDATED]
      }
    }
  }

  setIsDirty() {
    this.dirty = this.areRowsDeleted || this.rows.filter(el => el.status !== UserState[UserState.PERSISTED]).length > 0;

    this.enableSave = this.dirty;
    this.enableCancel = this.dirty;
  }

  buttonDelete() {
    this.deleteUsers(this.selected);
  }

  buttonDeleteAction(row) {
    this.deleteUsers([row]);
  }

  private deleteUsers(users: UserResponseRO[]) {
    if (this.isLoggedInUserSelected(users)) {
      this.alertService.error('You cannot delete the logged in user: ' + this.securityService.getCurrentUser().username);
      return;
    }

    this.enableDelete = false;
    this.enableEdit = false;

    for (const itemToDelete of users) {
      if (itemToDelete.status === UserState[UserState.NEW]) {
        this.rows.splice(this.rows.indexOf(itemToDelete), 1);
      } else {
        itemToDelete.status = UserState[UserState.REMOVED];
        itemToDelete.deleted = true;
      }
    }

    this.selected = [];
    this.areRowsDeleted = true;
    this.setIsDirty();
  }

  private disableSelectionAndButtons() {
    this.selected = [];
    this.enableCancel = false;
    this.enableSave = false;
    this.enableEdit = false;
    this.enableDelete = false;
  }

  public search() {
    this.getUsers();
  }

  async cancel() {
    const cancel = await this.dialogsService.openCancelDialog();
    if (cancel) {
      this.disableSelectionAndButtons();
      super.rows = [];
      super.count = 0;
      this.getUsers();
    }
  }

  async save(): Promise<boolean> {
    try {
      const isValid = this.userValidatorService.validateUsers(this.rows);
      if (!isValid) return false;

      const save = await this.dialogsService.openSaveDialog();
      if (save) {
        this.disableSelectionAndButtons();
        const modifiedUsers = this.rows.filter(el => el.status !== UserState[UserState.PERSISTED]);
        this.isBusy = true;
        await this.http.put(UserComponent.USER_USERS_URL, modifiedUsers).toPromise();
        this.isBusy = false;
        this.getUsers();
        this.alertService.success('The operation \'update users\' completed successfully.', false);
        return true;
      }
    } catch (err) {
      this.isBusy = false;
      this.getUsers();
      this.alertService.exception('The operation \'update users\' completed with errors.', err);
    }
    return false;
  }

  public get csvUrl(): string {
    return UserComponent.USER_CSV_URL;
  }

  isDirty(): boolean {
    return this.enableCancel;
  }

  page() {

  }

  // changePageSize(newPageLimit: number) {
  //   this.rowLimiter.pageSize = newPageLimit;
  //   this.disableSelectionAndButtons();
  //   super.rows = [];
  //   super.count = 0;
  //   this.getUsers();
  // }

  // onChangePage(event: any): void {
  //   this.setPage(event.offset);
  // }

  setPage(offset: number): void {
    super.offset = offset;
  }

  getLastPage(): number {
    if (!this.rows || !this.rowLimiter || !this.rowLimiter.pageSize)
      return 0;
    return Math.floor(this.rows.length / this.rowLimiter.pageSize);
  }

  onActivate(event) {
    if ('dblclick' === event.type) {
      this.buttonEdit();
    }
  }

  setState() {
    this.filter.deleted_notSet = this.filter.i++ % 3 === 1;
    if (this.filter.deleted_notSet) {
      this.filter.deleted = true;
    }
  }
}
