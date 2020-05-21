<template>
    <fragment v-if="isVisible">
        <slot></slot>
    </fragment>
</template>

<script>
    import {mapGetters} from 'vuex';
    import {Fragment} from 'vue-fragment';

    export default {
        name: "AccessControl",
        components: {'fragment': Fragment},
        props: {
            admin: {
                type: Boolean,
                default: false,
            },
            editor: {
                type: Boolean,
                default: false
            },
            guestOnly: {
                type: Boolean,
                default: false,
            }
        },
        computed: {
            ...mapGetters(['isAdmin', 'isEditor']),
            isVisible() {
                if (this.admin) {
                    return this.isAdmin;
                }
                if (this.editor) {
                    return this.isEditor;
                }
                if (this.guestOnly) {
                    return !this.isAdmin && !this.isEditor
                }
                return true;
            }
        }
    }
</script>

<style scoped>

</style>