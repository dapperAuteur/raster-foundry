import angular from 'angular';
import DiagramContainerComponent from './diagramContainer.component.js';
import DiagramContainerController from './diagramContainer.controller.js';

require('./diagramContainer.scss');

const DiagramContainerModule = angular.module('components.diagramContainer', []);

DiagramContainerModule.component('rfDiagramContainer', DiagramContainerComponent);
DiagramContainerModule.controller('DiagramContainerController', DiagramContainerController);

export default DiagramContainerModule;
