import {ContainerMixin, ElementMixin} from "vue-slicksort";
import "./styles.scss"
import { VImg }  from "vuetify/lib/index";

const SortableList = {
    mixins: [ContainerMixin],
    template: `
        <div class="list">
            <slot/>
        </div>
    `
};

const SortableItem = {
    mixins: [ElementMixin],
    props: ['item'],
    components: { VImg },
    template: `
        <div class="drag-sorter-item">
            <v-img :alt="item.id" :src="item.src"/>
        </div>
    `
};

const Sorter = {
    name: 'Sorter',
    template: `
        <div class="sorter__root">
        <SortableList :value="items" v-model="items" axis="xy" class="drag-sorter-list" :hideSortableGhost="false" >
            <SortableItem v-for="(item, index) in items" :index="index" :key="index"
                :item="item"
            />
        </SortableList>
        </div>
    `,
    props: ["value"],
    components: { SortableItem, SortableList },
    data() {
        return {
            items: this.value,
        }
    },
    watch: {
        items(newValue) {
            this.$emit("input", newValue)
        }
    }
};

export default Sorter