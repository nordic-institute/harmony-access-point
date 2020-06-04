import {AfterViewChecked, AfterViewInit, ChangeDetectorRef, Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {UserResponseRO, UserState} from './support/user';
import {UserSearchCriteria, UserService} from './support/user.service';
import {MAT_CHECKBOX_CLICK_ACTION, MatDialog} from '@angular/material';
import {UserValidatorService} from 'app/user/support/uservalidator.service';
import {AlertService} from '../common/alert/alert.service';
import {EditUserComponent} from 'app/user/edituser-form/edituser-form.component';
import {HttpClient} from '@angular/common/http';
import {SecurityService} from '../security/security.service';
import {DomainService} from '../security/domain.service';
import {Domain} from '../security/domain';
import {DialogsService} from '../common/dialogs/dialogs.service';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/mixins/base-list.component';
import FilterableListMixin from '../common/mixins/filterable-list.mixin';
import ModifiableListMixin from '../common/mixins/modifiable-list.mixin';
import {ClientPageableListMixin} from '../common/mixins/pageable-list.mixin';
import {ApplicationContextService} from '../common/application-context.service';

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
  implements OnInit, AfterViewInit, AfterViewChecked {

  static readonly USER_URL: string = 'rest/user';
  static readonly USER_USERS_URL: string = UserComponent.USER_URL + '/users';
  static readonly USER_CSV_URL: string = UserComponent.USER_URL + '/csv';

  @ViewChild('editableTpl', {static: false}) editableTpl: TemplateRef<any>;
  @ViewChild('checkBoxTpl', {static: false}) checkBoxTpl: TemplateRef<any>;
  @ViewChild('deletedTpl', {static: false}) deletedTpl: TemplateRef<any>;
  @ViewChild('rowActions', {static: false}) rowActions: TemplateRef<any>;

  userRoles: Array<String>;
  domains: Domain[];
  domainsPromise: Promise<Domain[]>;
  currentDomain: Domain;

  currentUser: UserResponseRO;
  editedUser: UserResponseRO;
  areRowsDeleted: boolean;
  deletedStatuses: any[];

  constructor(private applicationService: ApplicationContextService, private http: HttpClient, private userService: UserService,
              public dialog: MatDialog, private dialogsService: DialogsService, private userValidatorService: UserValidatorService,
              private alertService: AlertService, private securityService: SecurityService, private domainService: DomainService,
              private changeDetector: ChangeDetectorRef) {
    super();
  }

  async ngOnInit() {
    super.ngOnInit();

    super.filter = new UserSearchCriteria();
    this.deletedStatuses = [null, true, false];
    this.userRoles = [];
    this.currentUser = null;
    this.editedUser = null;
    this.domainService.getCurrentDomain().subscribe((domain: Domain) => this.currentDomain = domain);
    this.getUserRoles();
    this.areRowsDeleted = false;
    this.filterData();
  }

  public get name(): string {
    return 'Users';
  }

  async ngAfterViewInit() {
    this.columnPicker.allColumns = [
      {
        cellTemplate: this.editableTpl,
        name: 'Username',
        prop: 'userName',
        canAutoResize: true,
        showInitially: true
      },
      {
        cellTemplate: this.editableTpl,
        name: 'Role',
        prop: 'roles',
        canAutoResize: true,
        showInitially: true
      },
      {
        cellTemplate: this.editableTpl,
        name: 'Email',
        prop: 'email',
        canAutoResize: true,
        showInitially: false
      },
      {
        cellTemplate: this.checkBoxTpl,
        name: 'Active',
        canAutoResize: true,
        width: 25,
        showInitially: true
      },
      {
        cellTemplate: this.deletedTpl,
        name: 'Deleted',
        canAutoResize: true,
        width: 25,
        showInitially: true
      },
      {
        cellTemplate: this.rowActions,
        name: 'Actions',
        width: 60,
        canAutoResize: true,
        sortable: false,
        showInitially: true
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
          canAutoResize: true,
          showInitially: true
        });
    }

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => col.showInitially);
  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }

  public async getDataAndSetResults(): Promise<any> {
    return this.getUsers();
  }

  async getUsers(): Promise<any> {
    return this.userService.getUsers(this.activeFilter).toPromise().then(async users => {
      await this.userService.checkConfiguredCorrectlyForMultitenancy(users);

      await this.setDomain(users);

      super.rows = users;
      super.count = users.length;

      this.areRowsDeleted = false;
      this.disableSelection();
    });
  }

  private async setDomain(users: UserResponseRO[]) {
    const showDomain = await this.userService.isDomainVisible();
    if (showDomain) {
      await this.getUserDomains();
      users.forEach(user => this.setDomainName(user));
    }
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
    this.currentUser = this.selected[0];
    this.editedUser = this.currentUser;
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

  add(): void {
    if (this.isBusy()) {
      return;
    }

    this.setPage(this.getLastPage());

    this.editedUser = new UserResponseRO('', this.currentDomain, '', '', true, UserState[UserState.NEW], [], false, false);
    this.setIsDirty();
    this.dialog.open(EditUserComponent, {
      data: {
        user: this.editedUser,
        userroles: this.userRoles,
        userdomains: this.domains
      }
    }).afterClosed().subscribe(ok => {
      if (ok) {
        super.rows = [...this.rows, this.editedUser];
        super.count = this.count + 1;
        this.currentUser = this.editedUser;
      } else {
        super.selected = [];
      }
      this.setIsDirty();
    });
  }

  edit() {
    if (this.currentUser && this.currentUser.deleted) {
      this.alertService.error('You cannot edit a deleted user.', false, 5000);
      return;
    }
    this.editUser(this.currentUser);
  }

  editUser(currentUser) {
    if (this.isLoading) {
      return;
    }

    const rowCopy = Object.assign({}, currentUser);
    this.dialog.open(EditUserComponent, {
      data: {
        user: rowCopy,
        userroles: this.userRoles,
        userdomains: this.domains
      }
    }).afterClosed().subscribe(ok => {
      if (ok) {
        if (JSON.stringify(currentUser) !== JSON.stringify(rowCopy)) {
          Object.assign(currentUser, rowCopy);
          if (currentUser.status == UserState[UserState.PERSISTED]) {
            currentUser.status = UserState[UserState.UPDATED]
          }
          this.setIsDirty();
        }
      }
    });
  }

  setIsDirty() {
    super.isChanged = this.areRowsDeleted || this.rows.filter(el => el.status !== UserState[UserState.PERSISTED]).length > 0;
  }

  delete() {
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

    for (const itemToDelete of users) {
      if (itemToDelete.status === UserState[UserState.NEW]) {
        this.rows.splice(this.rows.indexOf(itemToDelete), 1);
      } else {
        itemToDelete.status = UserState[UserState.REMOVED];
        itemToDelete.deleted = true;
      }
    }

    super.selected = [];
    this.areRowsDeleted = true;
    this.setIsDirty();
  }

  private disableSelection() {
    super.selected = [];
  }

  async doSave(): Promise<any> {
    const isValid = this.userValidatorService.validateUsers(this.rows);
    if (!isValid) {
      return false;
    }

    const modifiedUsers = this.rows.filter(el => el.status !== UserState[UserState.PERSISTED]);
    return this.http.put(UserComponent.USER_USERS_URL, modifiedUsers).toPromise().then(() => {
      this.loadServerData();
    });
  }

  get csvUrl(): string {
    return UserComponent.USER_CSV_URL;
  }

  setState() {
    this.filter.deleted_notSet = this.filter.i++ % 3 === 1;
    if (this.filter.deleted_notSet) {
      this.filter.deleted = true;
    }
  }

  canEdit() {
    return this.oneRowSelected() && this.selectedRowNotDeleted() && !this.isBusy();
  }

  canDelete() {
    return this.atLeastOneRowSelected() && this.notEveryRowIsDeleted() && !this.isBusy();
  }

  private notEveryRowIsDeleted() {
    return !this.selected.every(el => el.deleted);
  }

  private atLeastOneRowSelected() {
    return this.selected.length > 0;
  }

  private selectedRowNotDeleted() {
    return !this.selected[0].deleted;
  }

  private oneRowSelected() {
    return this.selected.length === 1;
  }

}
