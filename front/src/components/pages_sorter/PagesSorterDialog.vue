<template>
    <v-dialog v-model="opened" fullscreen>
        <v-card>
            <v-card-title>
                Series editor
            </v-card-title>
            <v-card-text>
                <sorter />
            </v-card-text>
            <v-card-actions>
                <v-btn color="primary" @click="commit" text>Confirm</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script>
    import Sorter from "./PagesSorter";

    export default {
        name: "PagesSorterDialog",
        components: {Sorter},
        props: ['value'],
        data() {
            return {
                opened: this.value,
                images: [],
            }
        },
        methods: {
            commit() {
                this.$emit("input", false)
            },
        },
        watch: {
            value(val) {
                this.opened = val;
            }
        }
    }
</script>

<style scoped>

</style>