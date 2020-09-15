import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from './security/login/login.component';
import {AuthenticatedAuthorizedGuard} from './common/guards/authenticated-authorized.guard';
import {ErrorLogComponent} from './errorlog/errorlog.component';
import {CurrentPModeComponent} from './pmode/current/currentPMode.component';
import {PModeArchiveComponent} from './pmode/archive/pmodeArchive.component';
import {MessageFilterComponent} from './messagefilter/messagefilter.component';
import {MessageLogComponent} from './messagelog/messagelog.component';
import {UserComponent} from './user/user.component';
import {TruststoreComponent} from 'app/truststore/truststore.component';
import {JmsComponent} from './jms/jms.component';
import {DirtyGuard} from './common/guards/dirty.guard';
import {AuditComponent} from './audit/audit.component';
import {PartyComponent} from './party/party.component';
import {AlertsComponent} from './alerts/alerts.component';
import {PluginUserComponent} from './pluginuser/pluginuser.component';
import {DefaultPasswordGuard} from './security/defaultPassword.guard';
import {AuthExternalProviderGuard} from './common/guards/auth-external-provider.guard';
import {LoggingComponent} from './logging/logging.component';
import {ChangePasswordComponent} from './security/change-password/change-password.component';
import {LogoutAuthExtProviderComponent} from "./security/logout/logout.components";
import {RedirectHomeGuard} from "./common/guards/redirect-home.guard";
import {NotAuthorizedComponent} from "./security/not-authorized/not-authorized.components";
import {SecurityService} from "./security/security.service";
import {PropertiesComponent} from './properties/properties.component';
import {ConnectionsComponent} from './testservice/connections.component';
import {AuthInternalProviderGuard} from './common/guards/auth-internal-provider.guard';

const appRoutes: Routes = [
  {
    path: '',
    component: MessageLogComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    data: {
      checkRoles: SecurityService.USER_ROLES,
      helpPage: 'Messages'
    },
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'pmode-current',
    component: CurrentPModeComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    canDeactivate: [DirtyGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES,
      helpPage: 'PMode-Current'
    },
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'pmode-archive',
    component: PModeArchiveComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    canDeactivate: [DirtyGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES,
      helpPage: 'PMode-Archive'
    },
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'pmode-party',
    component: PartyComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    canDeactivate: [DirtyGuard],
    data: {
      checkRoles: SecurityService.USER_ROLES,
      helpPage: 'PMode-Parties'
    },
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'jms',
    component: JmsComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    canDeactivate: [DirtyGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES,
      helpPage: 'JMSMonitoring'
    },
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'messagefilter',
    component: MessageFilterComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    canDeactivate: [DirtyGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES,
      helpPage: 'MessageFilter'
    },
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'truststore',
    component: TruststoreComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES,
      helpPage: 'Truststore'
    },
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'messagelog',
    component: MessageLogComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    data: {
      checkRoles: SecurityService.USER_ROLES,
      helpPage: 'Messages'
    },
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'user',
    component: UserComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard, AuthExternalProviderGuard],
    canDeactivate: [DirtyGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES,
      helpPage: 'Users'
    },
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'pluginuser',
    component: PluginUserComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    canDeactivate: [DirtyGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES,
      helpPage: 'PluginUsers'
    },
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'errorlog',
    component: ErrorLogComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    data: {
      checkRoles: SecurityService.USER_ROLES,
      helpPage: 'ErrorLog'
    },
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [AuthExternalProviderGuard, RedirectHomeGuard],
    data: {
      helpPage: 'Login'
    },
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'audit',
    component: AuditComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES,
      helpPage: 'Audit'
    },
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'alerts',
    component: AlertsComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    canDeactivate: [DirtyGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES,
      helpPage: 'Alerts'
    },
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'connections',
    component: ConnectionsComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES,
      helpPage: 'ConnectionMonitoring'
    },
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'changePassword',
    component: ChangePasswordComponent,
    canActivate: [AuthenticatedAuthorizedGuard, AuthExternalProviderGuard],
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'logging',
    component: LoggingComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    data: {
      isDomainIndependent: true,
      checkRoles: SecurityService.ADMIN_ROLES,
      helpPage: 'Logging'
    },
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'properties',
    component: PropertiesComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES,
      helpPage: 'Properties'
    },
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'logout',
    component: LogoutAuthExtProviderComponent,
    canActivate: [AuthInternalProviderGuard],
    data: {
      isDomainIndependent: true
    },
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'notAuthorized',
    component: NotAuthorizedComponent,
    canActivate: [AuthenticatedAuthorizedGuard],
    runGuardsAndResolvers: 'always'
  },
  {
    path: '**',
    component: MessageLogComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    data: {
      checkRoles: SecurityService.USER_ROLES
    },
    runGuardsAndResolvers: 'always'
  },

];

export const routing = RouterModule.forRoot(appRoutes, {onSameUrlNavigation: 'reload'});
