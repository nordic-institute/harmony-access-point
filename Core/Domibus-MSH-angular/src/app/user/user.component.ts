import {
  AfterViewChecked,
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  OnInit,
  TemplateRef,
  ViewChild
} from '@angular/core';
import {UserResponseRO, UserState} from './support/user';
import {UserSearchCriteria, UserService} from './support/user.service';
import {UserValidatorService} from 'app/user/support/uservalidator.service';
import {AlertService} from '../common/alert/alert.service';
import {EditUserComponent} from 'app/user/edituser-form/edituser-form.component';
import {HttpClient, HttpParams} from '@angular/common/http';
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
import {ComponentName} from '../common/component-name-decorator';

@Component({
  templateUrl: 'user.component.html',
  styleUrls: ['./user.component.css'],
  providers: []
})
@ComponentName('Users')
export class UserComponent extends mix(BaseListComponent)
  .with(FilterableListMixin, ModifiableListMixin, ClientPageableListMixin)
  implements OnInit, AfterViewInit, AfterViewChecked {

  static readonly USER_URL: string = 'rest/user';
  static readonly USER_USERS_URL: string = UserComponent.USER_URL + '/users';
  static readonly USER_CSV_URL: string = UserComponent.USER_URL + '/csv';

  @ViewChild('editableTpl') editableTpl: TemplateRef<any>;
  @ViewChild('checkBoxTpl') checkBoxTpl: TemplateRef<any>;
  @ViewChild('deletedTpl') deletedTpl: TemplateRef<any>;
  @ViewChild('rowActions') rowActions: TemplateRef<any>;
  @ViewChild('rowWithDateFormatTpl') public rowWithDateFormatTpl: TemplateRef<any>;

  userRoles: Array<String>;
  domains: Domain[];
  domainsPromise: Promise<Domain[]>;
  currentDomain: Domain;

  currentUser: UserResponseRO;
  editedUser: UserResponseRO;
  areRowsDeleted: boolean;
  deletedStatuses: any[];
  allUsers: UserResponseRO[];

  constructor(private applicationService: ApplicationContextService, private http: HttpClient, private userService: UserService,
              private dialogsService: DialogsService, private userValidatorService: UserValidatorService,
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

  async ngAfterViewInit() {
    this.columnPicker.allColumns = [
      {
        cellTemplate: this.editableTpl,
        name: 'User Name',
        prop: 'userName',
        canAutoResize: true,
        showInitially: true,
        width: 200,
        minWidth: 190,
      },
      {
        cellTemplate: this.editableTpl,
        name: 'Role',
        prop: 'roles',
        canAutoResize: true,
        showInitially: true,
        width: 150,
        minWidth: 140,
      },
      {
        cellTemplate: this.editableTpl,
        name: 'Email',
        prop: 'email',
        canAutoResize: true,
        showInitially: false,
        width: 200,
        minWidth: 190,
      },
      {
        cellTemplate: this.checkBoxTpl,
        name: 'Active',
        canAutoResize: true,
        width: 100,
        minWidth: 90,
        showInitially: true
      },
      {
        cellTemplate: this.deletedTpl,
        name: 'Deleted',
        canAutoResize: true,
        width: 70,
        minWidth: 60,
        showInitially: false
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Expiration Date',
        prop: 'expirationDate',
        canAutoResize: true,
        showInitially: true,
        width: 200,
        minWidth: 190,
      },
      {
        cellTemplate: this.rowActions,
        name: 'Actions',
        width: 100,
        minWidth: 90,
        canAutoResize: true,
        sortable: false,
        showInitially: true
      },
    ];

    const showDomain = await this.userService.isDomainVisible();
    if (showDomain) {
      this.getUserDomains();

      this.columnPicker.allColumns.splice(2, 0,
        {
          cellTemplate: this.editableTpl,
          name: 'Domain',
          prop: 'domainName',
          width: 120,
          minWidth: 110,
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
    const userSearchCriteria = this.buildUserSearchCriteria();

    return this.userService.getUsers(userSearchCriteria)
      .then(async users => {
        this.allUsers = users;

        await this.userService.checkConfiguredCorrectlyForMultitenancy(users);

        await this.setDomain(users);

        super.rows = users;
        super.count = users.length;

        this.areRowsDeleted = false;
        this.disableSelection();
      });
  }

  private buildUserSearchCriteria(): UserSearchCriteria {
    const { authRole, userName, deleted, deleted_notSet } = this.activeFilter || {};

    return Object.assign(new UserSearchCriteria(), {
      authRole: authRole || undefined,
      userName: userName || undefined,
      deleted: deleted === true,
      deleted_notSet: deleted_notSet === true
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

    this.editedUser = new UserResponseRO('', this.currentDomain, '', '', true, UserState[UserState.NEW], [], false, false, null);
    this.setIsDirty();
    this.dialogsService.open(EditUserComponent, {
      data: {
        user: this.editedUser,
        userroles: this.userRoles,
        userdomains: this.domains
      }
    }).afterClosed().subscribe(ok => {
      if (ok) {
        super.rows = [...this.rows, this.editedUser];
        this.allUsers.push(this.editedUser);
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
    this.dialogsService.open(EditUserComponent, {
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
        this.allUsers.splice(this.allUsers.indexOf(itemToDelete), 1);
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
    try {
      this.userValidatorService.validateUsers(this.allUsers);

      const modifiedUsers = this.rows.filter(el => el.status !== UserState[UserState.PERSISTED]);
      return this.http.put(UserComponent.USER_USERS_URL, modifiedUsers).toPromise().then(() => {
        this.loadServerData();
      });
    } catch (ex) {
      this.alertService.exception('Cannot save users:', ex);
      return Promise.reject(ex);
    }
  }

  protected createAndSetParameters(): HttpParams {
    let filterParams = super.createAndSetParameters();
    if (this.filter.deleted_notSet) {
      filterParams = filterParams.set('deleted', 'all');
    }
    filterParams = filterParams.append('page', '0');
    filterParams = filterParams.append('pageSize', '10000');
    return filterParams;
  }

  get csvUrl(): string {
    return UserComponent.USER_CSV_URL + '?' + this.createAndSetParameters();
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
