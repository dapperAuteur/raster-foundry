import angular from 'angular';
import ProjectNavMenuComponent from './projectNavMenu.component.js';
import ProjectNavMenuController from './projectNavMenu.controller.js';

const ProjectNavMenuModule = angular.module('components.projectNavMenu', []);

ProjectNavMenuModule.component('projectNavMenu', ProjectNavMenuComponent);
ProjectNavMenuModule.controller('ProjectNavMenuController', ProjectNavMenuController);

export default ProjectNavMenuModule;
