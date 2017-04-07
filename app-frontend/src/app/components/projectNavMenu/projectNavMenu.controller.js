export default class ProjectNavMenuController {
    constructor(projectService, $state, $uibModal) {
        'ngInject';

        this.projectService = projectService;
        this.$state = $state;
        this.$uibModal = $uibModal;
    }

    selectProjectModal() {
        if (this.activeModal) {
            this.activeModal.dismiss();
        }

        this.activeModal = this.$uibModal.open({
            component: 'rfSelectProjectModal',
            resolve: {
                project: () => this.projectService.currentProject
            }
        });

        this.activeModal.result.then(p => {
            this.$state.go(this.$state.$current, { projectid: p.id });
        });

        return this.activeModal;
    }
}
