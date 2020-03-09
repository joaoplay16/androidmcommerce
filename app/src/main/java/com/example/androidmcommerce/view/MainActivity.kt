package com.example.androidmcommerce.view

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidmcommerce.R
import com.example.androidmcommerce.data.NavMenuItemsDataBase
import com.example.androidmcommerce.domain.NavMenuItem
import com.example.androidmcommerce.domain.User
import com.example.androidmcommerce.util.NavMenuItemDetailsLookup
import com.example.androidmcommerce.util.NavMenuItemKeyProvider
import com.example.androidmcommerce.util.NavMenuItemPredicate
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_user_logged.*
import kotlinx.android.synthetic.main.nav_header_user_not_logged.*
import kotlinx.android.synthetic.main.nav_menu.*

class MainActivity : AppCompatActivity() {

    val user = User(
        "João Player",
        R.drawable.user,
        false
    )

    lateinit var navMenuItems : List<NavMenuItem>
    lateinit var selectNavMenuItems: SelectionTracker<Long>

    lateinit var navMenuItemsLogged: List<NavMenuItem>
    lateinit var selectNavMenuItemsLogged: SelectionTracker<Long>
    lateinit var navMenu: NavMenuItemsDataBase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        initNavMenu(savedInstanceState)
    }


    override fun onSaveInstanceState( outState: Bundle) {
        super.onSaveInstanceState( outState )

        /*
         * Para manter o item de menu gaveta selecionado caso
         * haja reconstrução de atividade.
         * */
        selectNavMenuItems.onSaveInstanceState( outState )
        selectNavMenuItemsLogged.onSaveInstanceState( outState )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
                // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }



    private fun initNavMenu(savedInstanceState: Bundle?){
        navMenu = NavMenuItemsDataBase( this )
        navMenuItems = navMenu.items
        navMenuItemsLogged = navMenu.itemsLogged

        showHideNavMenuViews()

        initNavMenuItems()
        initNavMenuItemsLogged()

        if( savedInstanceState != null ){
            selectNavMenuItems.onRestoreInstanceState( savedInstanceState )
            selectNavMenuItemsLogged.onRestoreInstanceState( savedInstanceState )
        }else{
            selectNavMenuItems.select(R.id.item_all_shoes.toLong())
        }

    }

    private fun showHideNavMenuViews(){
        if( user.status ){ /* Conectado */
            rl_header_user_not_logged.visibility = View.GONE
            fillUserHeaderNavMenu()
        }
        else{  /* Não conectado */
            rl_header_user_logged.visibility = View.GONE
            v_nav_vertical_line.visibility = View.GONE
            rv_menu_items_logged.visibility = View.GONE
        }
    }
    private fun fillUserHeaderNavMenu(){
        if( user.status ) { /* Conectado */
            iv_user.setImageResource(user.image)
            tv_user.text = user.name
        }
    }

    private fun initNavMenuItems() {
        rv_menu_items.setHasFixedSize(false)
        rv_menu_items.layoutManager = LinearLayoutManager(this)
        rv_menu_items.adapter = NavMenuItemsAdapter(navMenuItems)

        initNavMenuItemsSelection()
    }

        private fun initNavMenuItemsSelection(){

        selectNavMenuItems = SelectionTracker.Builder<Long>(
                "id-selected-item",
                rv_menu_items,
                NavMenuItemKeyProvider( navMenuItems ),
                NavMenuItemDetailsLookup( rv_menu_items ),
                StorageStrategy.createLongStorage()
            )
            .withSelectionPredicate( NavMenuItemPredicate( this ) )
            .build()

        selectNavMenuItems.addObserver(
            SelectObserverNavMenuItems {
                selectNavMenuItemsLogged.selection.filter {
                    selectNavMenuItemsLogged.deselect( it )
                }
            }
        )

        (rv_menu_items.adapter as NavMenuItemsAdapter).selectionTracker = selectNavMenuItems
    }

    private fun initNavMenuItemsLogged(){
        rv_menu_items_logged.setHasFixedSize( true )
        rv_menu_items_logged.layoutManager = LinearLayoutManager( this )
        rv_menu_items_logged.adapter = NavMenuItemsAdapter( navMenuItemsLogged )

        initNavMenuItemsLoggedSelection()
    }

    private fun initNavMenuItemsLoggedSelection(){

        selectNavMenuItemsLogged = SelectionTracker.Builder<Long>(
                "id-selected-item-logged",
                rv_menu_items_logged,
                NavMenuItemKeyProvider( navMenuItemsLogged ),
                NavMenuItemDetailsLookup( rv_menu_items_logged ),
                StorageStrategy.createLongStorage()
            )
            .withSelectionPredicate( NavMenuItemPredicate( this ) )
            .build()

        selectNavMenuItemsLogged.addObserver(
            SelectObserverNavMenuItems {
                selectNavMenuItems.selection.filter {
                    selectNavMenuItems.deselect( it )
                }
            }
        )

        (rv_menu_items_logged.adapter as NavMenuItemsAdapter).selectionTracker = selectNavMenuItemsLogged
    }

    inner class SelectObserverNavMenuItems(
        val callbackRemoveSelection: ()->Unit
    ) : SelectionTracker.SelectionObserver<Long>(){

        /*
         * Método responsável por permitir que seja possível
         * disparar alguma ação de acordo com a mudança de
         * status de algum item em algum dos objetos de seleção
         * de itens de menu gaveta. Aqui vamos proceder com
         * alguma ação somente em caso de item obtendo seleção,
         * para item perdendo seleção não haverá processamento,
         * pois este status não importa na lógica de negócio
         * deste método.
         * */
        override fun onItemStateChanged(
            key: Long,
            selected: Boolean ) {
            super.onItemStateChanged( key, selected )

            /*
             * Padrão Cláusula de Guarda para não seguirmos
             * com o processamento em caso de item perdendo
             * seleção. O processamento posterior ao condicional
             * abaixo é somente para itens obtendo a seleção,
             * selected = true.
             * */
            if( !selected ){
                return
            }

            /*
             * Para garantir que somente um item de lista se
             * manterá selecionado, é preciso acessar o objeto
             * de seleção da lista de itens de usuário conectado
             * para então remover qualquer possível seleção
             * ainda presente nela. Sempre haverá somente um
             * item selecionado, mas infelizmente o método
             * clearSelection() não estava respondendo como
             * esperado, por isso a estratégia a seguir.
             * */
            callbackRemoveSelection()

            /*
             * TODO: Mudança de Fragment
             * */

            /*
             * Fechando o menu gaveta.
             * */
            drawer_layout.closeDrawer( GravityCompat.START )
        }
    }
}