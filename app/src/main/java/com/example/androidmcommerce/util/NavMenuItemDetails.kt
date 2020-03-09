package com.example.androidmcommerce.util

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import com.example.androidmcommerce.domain.NavMenuItem

/*O construtor está com propriedades mutáveis que aceitam valores null e -1,
 pois objetos ItemDetails são reaproveitados dentro de instâncias ViewHolder
 do adapter do RecyclerView alvo. Essa é uma decisão de projeto, para não
 inflar a memória com inúmeros novos objetos ItemDetails.*/
class NavMenuItemDetails(
    var item: NavMenuItem? = null,
    var adapterPosition: Int = -1
) : ItemDetailsLookup.ItemDetails<Long>() {

    /*
     * Retorna a posição do adaptador do item
     * (ViewHolder.adapterPosition).
     * */
    override fun getPosition() = adapterPosition

    /*
     * Retorna a entidade que é a chave de seleção do item.
     * */
    override fun getSelectionKey() = item!!.id

    /*
     * Retorne "true" se o item tiver uma chave de seleção. Se true
     * não for retornado o item em foco (acionado pelo usuário) não
     * será selecionado.
     * */
    override fun inSelectionHotspot( e: MotionEvent) = true
}